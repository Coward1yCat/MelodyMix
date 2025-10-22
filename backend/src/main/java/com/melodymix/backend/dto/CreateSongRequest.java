package com.melodymix.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateSongRequest {

    @NotBlank(message = "歌曲标题不能为空")
    private String title;

    @NotBlank(message = "歌手不能为空")
    private String artist;

    private String album;
    private LocalDate releaseDate;
    @Min(value = 1, message = "时长必须大于0")
    private Integer duration;

    @NotBlank(message = "歌曲文件URL不能为空")
    private String fileUrl;

    private String coverUrl;
}
