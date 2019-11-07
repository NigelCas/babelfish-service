// Babelfish Frontend Service

// API Server URL
const API_SERVER_URL = '/babelfish-service';
const TRANSCRIPTION_SOCKET_URL = '/babelfish-service/socket';

// semantic ui settings
// speech to text transcription
$('#transcription-checkbox')
  .checkbox({
    beforeChecked: function () {
      if ($('#transcription-input-model').dropdown('get value') != 'NO_MODEL') {
        return true;
      } else {
        return false;
      }
    }
  });
$('#transcription-input-lang-search').dropdown();
$('#transcription-input-model').dropdown();
$('#transcription-output-accordion').accordion('refresh');
// translate
$('#translate-output-lang-search').dropdown();
$('.menu .item').tab();
$('#translate-output-accordion').accordion('refresh');
// text-to-speech 
$('#synthesis-output-voice-search').dropdown();
$('#synthesis-output-device').dropdown();
$('#synthesis-output-encoding').dropdown();
// modals
$('#error-modal').modal('hide');


// Babel fish server POST
async function postBabelFish(serviceUrl, serviceObj) {
  try {
    let rawResponse = await fetch(
      serviceUrl, {
        method: 'post',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify(serviceObj)
      });
    let responseJSON = await rawResponse.json();
    let content = JSON.stringify(responseJSON);
    var respObj = JSON.parse(content);
    console.log("postBabelFish() Results : \n" + content);
    return respObj;
  } catch (e) {
    console.log('postBabelFish fetch post translation operation: ' + e.message);
  }
}

// Babel fish server download POST Data
async function downloadBabelFishData(serviceUrl, serviceObj) {
  try {
    let responseAudioType;
    let responseDataStream = await fetch(
      serviceUrl, {
        method: 'post',
        headers: {
          'Content-Type': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify(serviceObj)
      }).then(async res => {
      if (!res.ok) {
        throw new Error(`${res.status} = ${res.statusText}`);
      }
      responseAudioType = res.headers.get('Content-Type');
      // response.body is a readable stream.
      // Calling getReader() gives us exclusive access to
      // the stream's content      
      var reader = res.body.getReader();
      // read() returns a promise that resolves
      // when a value has been received      
      const result = await reader.read();
      return result;
    });

    let responseBlob = new Blob([responseDataStream.value], {
      type: responseAudioType
    });
    return responseBlob;
  } catch (e) {
    console.log('postBabelFish fetch post translation operation: ' + e.message);
  }
}

// new transcription - initialize
async function postTranscriptionParams(audioLanguageCode, audioEncoding, audioStreamingModel,
  sampleRate, isProfanityFilterEnabled, isEnhancedEnabled) {
  var transcriptionConfig = ({
    "audioLanguageCode": audioLanguageCode,
    "targetAudioEncoding": audioEncoding,
    "audioStreamingModel": audioStreamingModel,
    "sampleRate": sampleRate,
    "isProfanityFilterEnabled": isProfanityFilterEnabled,
    "isEnhancedEnabled": isEnhancedEnabled
  });
  let response = await postBabelFish(
    API_SERVER_URL + '/speech_to_text/realtime/transcription/start',
    transcriptionConfig,
  );
}
// TODO new transcription - stop


// new translation
async function postTranslation(inputText, outputFormat, outputLanguage) {
  var translation = ({
    "inputText": inputText,
    "outputFormat": outputFormat,
    "outputLanguage": outputLanguage
  });
  let response = await postBabelFish(
    API_SERVER_URL + '/translator/translations',
    translation,
  );
  // clear loading translation placeholder
  $('#translate-output-text').removeClass();
  $('#translate-output-text').addClass('active content');
  // display translation
  $('#translate-output-text').append('<p id="output-placeholder">' + response.outputText + '</p>');
  // enable form
  $('#translate-input-form').removeClass();
  $('#translate-input-form').addClass('ui attached form segment');
  // enable form submit             
  $('#gcp-translate-submit-button').removeClass();
  $('#gcp-translate-submit-button').addClass('fluid ui vertical animated orange submit button');
  document.getElementById("gcp-translate-submit-button").style.pointerEvents = "auto";
}

// new text-to-speech synthesization
async function postSynthesization(audioDeviceProfile, audioEncoding, inputText,
  voiceLanguageName, voiceGender, voiceLanguageCode) {
  var synthesization = ({
    "audioDeviceProfile": audioDeviceProfile,
    "audioEncoding": audioEncoding,
    "inputData": inputText,
    "voiceLanguageName": voiceLanguageName,
    "voiceGender": voiceGender,
    "voiceLanguageCode": voiceLanguageCode
  });

  let response = await downloadBabelFishData(
    API_SERVER_URL + '/text_to_speech/synthesizations',
    synthesization,
  );
  // clear loading translation placeholder
  $('#synthesis-output-audio').removeClass();
  $('#synthesis-output-audio').addClass('ui fluid');
  // display translation
  $('#synthesis-output-audio').append('<p id="audio-output-placeholder">' +
    '    <audio controls src="' + window.URL.createObjectURL(response) + '"></audio></p>');
  // enable form
  $('#synthesis-input-form').removeClass();
  $('#synthesis-input-form').addClass('ui attached form segment');
  // enable form submit             
  $('#synthesis-submit-button').removeClass();
  $('#synthesis-submit-button').addClass('fluid ui vertical animated orange submit button');
  document.getElementById("synthesis-submit-button").style.pointerEvents = "auto";
}

// Transcription Form validation
$('#transcription-input-form')
  .form({
    fields: {
      transcriptionLang: {
        rules: [{
          type: 'empty',
          prompt: 'Please select the microphone language to transcribe to'
        }]
      },
      transcriptionInputModel: {
        rules: [{
          type: 'empty',
          prompt: 'Please select a model for audio transcription'
        }]
      }
      //      transcriptionUseEnhancedModel: {
      //        identifier: 'transcriptionUseEnhancedModel',
      //        rules: [{
      //          type: 'checked',
      //          prompt: 'Please select the microphone language to transcribe to'
      //        }]
      //      }
    },
    onSuccess: function (event, fields) {
      console.log('transcription-input-form submit: ' + event);
      // disable form submit
      event.preventDefault();
      // disable form submit button
      $('#transcription-submit-button').removeClass();
      $('#transcription-submit-button').addClass('ui orange loading submit button');
      document.getElementById("transcription-submit-button").style.pointerEvents = "none";
      // remove output placeholder
      $("#transcription-output-placeholder").text("");
      // disable form
      //      $('#transcription-input-form').removeClass();
      //      $('#transcription-input-form').addClass('ui attached form disabled segment');
      // show loading translation placeholder
      $('#transcription-output-text').removeClass();
      $('#transcription-output-text').addClass('fluid ui placeholder active content');
      // retrieve form data
      let selectedLanguageCode = $('#transcription-input-lang-search').dropdown('get value');
      let selectedTranscriptionModel = $('#transcription-input-model').dropdown('get value');
      let transcriptionUseEnhancedModel;
      $('#transcription-checkbox').checkbox().each(function () {
        if ($(this).checkbox('is checked')) {
          transcriptionUseEnhancedModel = true;
        } else {
          transcriptionUseEnhancedModel = false;
        }
      });

      // disable form controls
      $('#transcription-input-lang-search').removeClass();
      $('#transcription-input-lang-search').addClass('ui search disabled dropdown');
      $('#transcription-input-model').removeClass();
      $('#transcription-input-model').addClass('ui search disabled dropdown');
      $('#transcription-checkbox').removeClass();
      $('#transcription-checkbox').addClass('ui disabled checkbox');

      // Realtime Transcription - Start - pass parameters
      postTranscriptionParams(
        selectedLanguageCode,
        "OGG_OPUS",
        selectedTranscriptionModel,
        16000,
        false,
        transcriptionUseEnhancedModel);

      // get browser microphone media
      var constraints = {
        audio: {
          sampleRate: 16,
          channelCount: 1
        }
      };
      // Realtime Transcription - Start listener process
      getMicrophoneMedia(constraints);

      // postTranscriptionInit()

    }
  });


// Translate Form validation
$('#translate-input-form')
  .form({
    fields: {
      translationOutputLang: {
        rules: [{
          type: 'empty',
          prompt: 'Please select a language to translate to'
        }]
      },
      translationInput: {
        rules: [{
          type: 'empty',
          prompt: 'Please enter text to perform translation'
        }]
      }
    },
    onSuccess: function (event, fields) {
      console.log('translate-input-form submit: ' + event);
      // disable form submit
      event.preventDefault();
      $('#gcp-translate-submit-button').removeClass();
      $('#gcp-translate-submit-button').addClass('fluid ui loading vertical animated orange submit button');
      document.getElementById("gcp-translate-submit-button").style.pointerEvents = "none";
      //$("#gcp-translate-submit-button").click(function( event ) {event.stopPropagation();});
      $("#output-placeholder").remove();
      // disable form
      $('#translate-input-form').removeClass();
      $('#translate-input-form').addClass('ui attached form disabled segment');
      // show loading translation placeholder
      $('#translate-output-text').removeClass();
      $('#translate-output-text').addClass('fluid ui placeholder active content');
      // retrieve data from form
      let text = $('#input-text').val();
      let selectedLanguageText = $('#translate-output-lang-search').dropdown('get text');
      var selectedLanguageName = selectedLanguageText.split('[')[0].trim();
      // clear input fields
      //$('#translate-input-form').form('clear');
      postTranslation(text, 'TEXT', selectedLanguageName);
    }
  });

// synthesis Form validation
$('#synthesis-input-form')
  .form({
    fields: {
      synthesisOutputDevice: {
        rules: [{
          type: 'empty',
          prompt: 'Please select a device profile'
        }]
      },
      synthesisOutputVoice: {
        rules: [{
          type: 'empty',
          prompt: 'Please select a voice language:gender profile'
        }]
      },
      synthesisOutputEncoding: {
        rules: [{
          type: 'empty',
          prompt: 'Please select a audio encoding'
        }]
      },
      synthesisInputText: {
        rules: [{
          type: 'empty',
          prompt: 'Please enter text to perform a speech synthesis'
        }]
      }
    },
    onSuccess: function (event, fields) {
      // disable form submit
      event.preventDefault();
      $('#synthesis-submit-button').removeClass();
      $('#synthesis-submit-button').addClass('fluid ui loading vertical animated orange submit button');
      document.getElementById("synthesis-submit-button").style.pointerEvents = "none";
      // clear outputs
      $('#audio-output-placeholder').remove();
      // disable form
      $('#synthesis-input-form').removeClass();
      $('#synthesis-input-form').addClass('ui attached form disabled segment');
      // show loading translation placeholder
      $('#synthesis-output-audio').removeClass();
      $('#synthesis-output-audio').addClass('ui fluid placeholder');
      // retrieve data from form
      let text = $('#synthesis-input-text').val();
      let selectedDeviceProfile = $('#synthesis-output-device').dropdown('get value');
      let selectedLanguageCode = $('#synthesis-output-voice-search').dropdown('get value');
      let selectedVoiceProfile = $('#synthesis-output-voice-search').dropdown('get text');
      let selectedAudioEncoding = $('#synthesis-output-encoding').dropdown('get value');
      // Fetch synthesised audio
      postSynthesization(selectedDeviceProfile, selectedAudioEncoding, text,
        selectedVoiceProfile.split(':')[0].trim(), selectedVoiceProfile.split(':')[1].trim(),
        selectedLanguageCode);
    }
  });

// Fetch Supported audio transcription languages
async function getSpeechTranscriptionSupportedLanguages() {
  $('#transcription-input-form').removeClass();
  $('#transcription-input-form').addClass('ui loading attached form segment');
  try {
    let rawResponse = await fetch(
      API_SERVER_URL + '/speech_to_text/transcription/support/audio/languages', {
        method: 'get',
        headers: {
          'Accept': 'application/json'
        },
      });
    let data = await rawResponse.json();
    let content = JSON.stringify(data);
    var langObj = JSON.parse(content);
    console.log("Raw getTranslationSupportedLanguages() Results : \n" + content);
    var length = langObj._embedded.speechToTextSupportedAudioLanguagesResponseList.length;
    console.log("No. of Supported Translation Languages : \n" + length);

    let code;
    langObj._embedded.speechToTextSupportedAudioLanguagesResponseList.forEach((item) => {
      Object.entries(item).forEach(([key, val]) => {
        var newOption = $('<option/>');
        if (key === 'languageCode') {
          code = val;
        }
        if (key === 'languageAngloName') {
          newOption.attr('value', code);
          newOption.text(val);
          $('#transcription-input-lang-search').append(newOption);
        }
      });
    });
    $('#transcription-input-form').removeClass();
    $('#transcription-input-form').addClass('ui attached form segment');
    return content;
  } catch (e) {
    console.log('babelfish-service getSpeechTranscriptionSupportedLanguages() operation: ' + e.message);
  }
}

// Fetch Supported Translation languages
async function getTranslationSupportedLanguages() {
  $('#translate-input-form').removeClass();
  $('#translate-input-form').addClass('ui loading attached form segment');
  try {
    let rawResponse = await fetch(
      API_SERVER_URL + '/translator/translations/support/languages', {
        method: 'get',
        headers: {
          'Accept': 'application/json'
        },
      });
    let data = await rawResponse.json();
    let content = JSON.stringify(data);
    var langObj = JSON.parse(content);
    console.log("Raw getTranslationSupportedLanguages() Results : \n" + content);
    var length = langObj._embedded.languageList.length;
    console.log("No. of Supported Translation Languages : \n" + length);
    let code;

    langObj._embedded.languageList.forEach((item) => {
      Object.entries(item).forEach(([key, val]) => {
        var outputOption = $('<option/>');
        if (key === 'code') {
          code = val;
        }
        if (key === 'name') {
          outputOption.attr('value', code);
          outputOption.text(val + " [" + code + "]");
          $('#translate-output-lang-search').append(outputOption);
        }
      });
    });
    $('#translate-input-form').removeClass();
    $('#translate-input-form').addClass('ui attached form segment');
    return content;
  } catch (e) {
    console.log('babelfish-service supported language fetch list operation: ' + e.message);
  }
}

// Fetch Supported text-to-speech supported speech voice languages
async function getSpeechSynthesizationSupportedVoiceLanguages() {
  $('#synthesis-input-form').removeClass();
  $('#synthesis-input-form').addClass('ui loading attached form segment');
  try {
    let rawResponse = await fetch(
      API_SERVER_URL + '/text_to_speech/support/voices', {
        method: 'get',
        headers: {
          'Accept': 'application/json'
        },
      });
    let data = await rawResponse.json();
    let content = JSON.stringify(data);
    var langObj = JSON.parse(content);
    console.log("Raw getTranslationSupportedLanguages() Results : \n" + content);
    var length = langObj._embedded.textToSpeechSupportedVoicesResponseList.length;
    console.log("No. of Supported Synthesized Voice Languages : \n" + length);
    let code;
    let name;
    langObj._embedded.textToSpeechSupportedVoicesResponseList.forEach((item) => {
      Object.entries(item).forEach(([key, val]) => {
        var newOption = $('<option/>');
        if (key === 'languageCodes') {
          code = val[0];
        }
        if (key === 'name') {
          name = val;
        }
        if (key === 'ssmlGender') {
          newOption.attr('value', code);
          newOption.text(name + " : " + val);
          $('#synthesis-output-voice-search').append(newOption);
        }
      });
    });
    $('#synthesis-input-form').removeClass();
    $('#synthesis-input-form').addClass('ui attached form segment');
    return content;
  } catch (e) {
    console.log('babelfish-service supported language fetch list operation: ' + e.message);
  }
}

// load form data on load
getSpeechTranscriptionSupportedLanguages();
getSpeechSynthesizationSupportedVoiceLanguages();
getTranslationSupportedLanguages();







// 
// $('button#synthesis-start').click(function (event) {
//   $('button#synthesis-start').attr("disabled", true);
//   let text = $('textarea#synthesis-input').val();
//   let selectedVoice = $("#synth-voice-output-lang option:selected").html();
//   let textArray = selectedVoice.split(new RegExp(":").trim());
//   let translation = postSynthesization(text, textArray[1], textArray[0]);
//   $('li#synthesis-output').append("<p>" + translation + "</p>");
//   $('button#translation-start').removeAttr("disabled");
// });

function clearTranslateFields() {

}












// Websocket babelfish client
var stompClient = null;

function connectToBabelFishTranscriptionServer() {
  var socket = new SockJS(TRANSCRIPTION_SOCKET_URL);
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function (frame) {
    //setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/service/transcriptions/realtime/response', function (response) {
      // var message = JSON.parse(response.body).content;
      console.log('connectToBabelFishTranscriptionServer() - response : ' + response);
      showTranscription(response);
    });
  });
}

function disconnectToBabelFishTranscriptionServer() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  //setConnected(false);
  console.log("Disconnected");
}

function sendData(data) {
  stompClient.send("/app/realtime/transcription/transmit", {}, data);
  console.log('sendData() - sent request data : ' + data);
}

function showTranscription(message) {
  // clear loading translation placeholder
  $('#translate-output-text').removeClass();
  $('#translate-output-text').addClass('active content');
  var presentTranscription = $('#transcription-output-placeholder').text();
  $('#transcription-output-text').append('<p id="transcription-output-placeholder">' +
    presentTranscription + ' ' + message + '</p>');

}



// $(function () {
//     $("form").on('submit', function (e) {
//         e.preventDefault();
//     });
//     $( "#connect" ).click(function() { connect(); });
//     $( "#disconnect" ).click(function() { disconnect(); });
//     $( "#send" ).click(function() { sendName(); });
// });
var recorder = null

$('button#transcription-stop-button').click(function (event) {
  // disable form submit
  event.preventDefault();
  $('#transcription-stop-button').removeClass();
  $('#transcription-stop-button').addClass('ui disabled button');
  document.getElementById("transcription-stop-button").style.pointerEvents = "none";
  if (null != recorder) {
    if (recorder.state != "inactive") {
      recorder.state = 'inactive';
      recorder.stop();
      console.log("Microphone recorder status : " + recorder.state);
    } else {
      console.log("Microphone recorder already " + recorder.state);
    }
  } else {
    console.log("Microphone recorder not set...");
  }
  $('#transcription-stop-button').removeClass();
  $('#transcription-stop-button').addClass('ui button');
  document.getElementById("transcription-stop-button").style.pointerEvents = "auto";
});


async function getMicrophoneMedia(constraints) {
  let mediaStream = null;
  var chunks = [];
  try {
    // use FileReader to get Base64 encoded data
    const reader = new FileReader();
    connectToBabelFishTranscriptionServer();
    mediaStream = await navigator.mediaDevices.getUserMedia(constraints);
    console.log('getUserMedia supported by browser.');
    // use MediaStream Recording API
    recorder = new MediaRecorder(mediaStream);

    recorder.onstart = function () {
      console.log("onstart, Microphone recorder status : " + recorder.state);
    }
    recorder.onpause = function () {
      console.log("onpause, Microphone recorder status : " + recorder.state);
      //disconnectToBabelFishTranscriptionServer();
      var audioChunkBlob = new Blob(chunks, {
        'type': 'audio/ogg; codecs=opus'
      });
      var audioURL = window.URL.createObjectURL(audioChunkBlob);
      $("#microphone-audio-player").attr("src", audioURL);
      // convert to Base64 text
      reader.readAsDataURL(audioChunkBlob);
      reader.onload = function (evt) {
        var base64data = evt.target.result.toString();
        sendData(base64data);
        // console.log(base64data);
      }      
    }
    recorder.onresume = function () {
      console.log("onresume, Microphone recorder status : " + recorder.state);
    }
    recorder.onstop = function () {
      console.log("onstop, Microphone recorder status : " + recorder.state);
      //disconnectToBabelFishTranscriptionServer();
      var audioChunkBlob = new Blob(chunks, {
        'type': 'audio/ogg; codecs=opus'
      });
      var audioURL = window.URL.createObjectURL(audioChunkBlob);
      $("#microphone-audio-player").attr("src", audioURL);

      // convert to Base64 text
      reader.readAsDataURL(audioChunkBlob);
      reader.onload = function (evt) {
        var base64data = evt.target.result.toString();
        sendData(base64data);
        // console.log(base64data);
      }      

      // enable transcription form controls
      $('#transcription-submit-button').removeClass();
      $('#transcription-submit-button').addClass('ui orange submit button');
      document.getElementById("transcription-submit-button").style.pointerEvents = "none";
      $('#transcription-input-lang-search').removeClass();
      $('#transcription-input-lang-search').addClass('ui search dropdown');
      $('#transcription-input-model').removeClass();
      $('#transcription-input-model').addClass('ui search dropdown');
      $('#transcription-checkbox').removeClass();
      $('#transcription-checkbox').addClass('ui checkbox');
    }

    // fires every one second and passes an BlobEvent
    recorder.ondataavailable = function (event) {
      // append event data to array
      chunks.push(event.data);
    }

    recorder.onerror = function (event) {
      let error = event.error;
      switch (error.name) {
        case InvalidStateError:
          console.log("You can't record the audio right now. Try again later.");
          break;
        case SecurityError:
          console.log("Recording the specified source " +
            "is not allowed due to security " +
            "restrictions.");
          break;
        default:
          console.log("A problem occurred while trying to record the audio.");
          break;
      }
    };

    // make data available event fire every one second
    recorder.start(1000);
  } catch (err) {
    console.log('getUserMedia not supported by browser.');
    console.log("getMicrophoneMedia() : " + err.message);
  }
}