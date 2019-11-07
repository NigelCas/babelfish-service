package com.trabeya.engineering.babelfish.queue.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(callSuper=true)
public class TranscriptionDto {

    private String id;
    private String message;

}
