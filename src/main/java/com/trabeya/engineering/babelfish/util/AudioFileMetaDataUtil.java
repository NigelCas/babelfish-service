package com.trabeya.engineering.babelfish.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.audio.AudioParser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.gagravarr.tika.FlacParser;
import org.gagravarr.tika.OpusParser;
import org.gagravarr.tika.SpeexParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class AudioFileMetaDataUtil {

    AudioFileMetaDataUtil(){}

    private static String detectDocTypeUsingFacade(InputStream stream)
            throws IOException {
        Tika tika = new Tika();
        return tika.detect(stream);
    }

    public static Metadata listAudioMetaDataFromBytes(byte[] inputFileData){
        Metadata metadata = new Metadata();
        try {
            InputStream input = new ByteArrayInputStream(inputFileData);
            ContentHandler handler = new DefaultHandler();
            Parser parser = null;
            String detectedMime = detectDocTypeUsingFacade(input);

            switch (detectedMime){
                case "audio/mpeg": parser = new Mp3Parser(); break;
                case "audio/flac": parser = new FlacParser(); break;
                case "audio/ogg": parser = new OpusParser(); break;
                case "audio/speex": parser = new SpeexParser(); break;
                default : parser = new AudioParser(); break;
            }

            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
            input.close();

        } catch (Exception ex) {
            log.error("listMetaDataFromBytes error : ", ex);
        }
        return metadata;
    }

}
