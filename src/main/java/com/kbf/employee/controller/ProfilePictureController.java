package com.kbf.employee.controller;

import com.kbf.employee.dto.request.EmployeeDTO;
import com.kbf.employee.dto.request.ProfilePictureUploadDTO;
import com.kbf.employee.exception.AccessDeniedException;
import com.kbf.employee.exception.ResourceNotFoundException;
import com.kbf.employee.security.UserPrincipal;
import com.kbf.employee.service.EmployeeProfilePictureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Profile Picture", description = "Employee Profile Picture Management API")
@RestController
@RequestMapping("/api/profile-pictures")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProfilePictureController {

    private final EmployeeProfilePictureService profilePictureService;

    @Operation(summary = "Upload profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping(value = "/{employeeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Long employeeId,
            @ModelAttribute ProfilePictureUploadDTO uploadDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (!userPrincipal.isAdmin() && !employeeId.equals(userPrincipal.getId())) {
            throw new AccessDeniedException("You can only upload your own profile picture");
        }

        String filePath = profilePictureService.uploadProfilePicture(
                employeeId,
                uploadDTO.getProfilePicture()
        );

        return ResponseEntity.ok(filePath);
    }

    @Operation(summary = "Remove profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profile picture removed successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteProfilePicture(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (!userPrincipal.isAdmin() && !employeeId.equals(userPrincipal.getId())) {
            throw new AccessDeniedException("You can only remove your own profile picture");
        }

        profilePictureService.deleteProfilePicture(employeeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture retrieved"),
            @ApiResponse(responseCode = "404", description = "Profile picture not found")
    })
    @GetMapping("/{employeeId}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable Long employeeId) {
        if (employeeId == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Resource resource = profilePictureService.getProfilePicture(employeeId);
            String contentType = determineContentType(resource);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
}
    @Operation(summary = "Get profile picture thumbnail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture thumbnail retrieved"),
            @ApiResponse(responseCode = "404", description = "Profile picture thumbnail not found")
    })
    @GetMapping("/{employeeId}/thumbnail")
    public ResponseEntity<Resource> getProfilePictureThumbnail(@PathVariable Long employeeId) {
        Resource resource = profilePictureService.getProfilePictureThumbnail(employeeId);

        String contentType = determineContentType(resource);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @Operation(summary = "Update employee profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated with new profile picture"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping(value = "/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<EmployeeDTO> updateEmployeeProfilePicture(
            @PathVariable Long id,
            @ModelAttribute ProfilePictureUploadDTO uploadDTO) {

        log.info("Updating profile picture for employee ID: {}", id);
        EmployeeDTO updatedEmployee = profilePictureService.updateEmployeeProfilePicture(id, uploadDTO.getProfilePicture());
        return ResponseEntity.ok(updatedEmployee);
    }

    private String determineContentType(Resource resource) {
        try {
            String filename = resource.getFilename();
            if (filename != null) {
                if (filename.endsWith(".png")) return "image/png";
                if (filename.endsWith(".gif")) return "image/gif";
                if (filename.endsWith(".webp")) return "image/webp";
            }
        } catch (Exception e) {
            log.warn("Could not determine content type from filename", e);
        }
        return "image/jpeg"; // default
    }
}