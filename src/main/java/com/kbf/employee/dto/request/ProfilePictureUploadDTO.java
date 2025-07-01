package com.kbf.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Profile Picture Upload Request")
public class ProfilePictureUploadDTO {
    @Schema(description = "Profile picture file (JPEG, PNG, max 5MB)",
            required = true,
            format = "binary")
    private MultipartFile profilePicture;
}