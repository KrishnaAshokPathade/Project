package com.backend.controller;


import com.backend.payload.ImageResponce;
import com.backend.payload.PagableResponce;
import com.backend.payload.UserDto;
import com.backend.service.FileService;
import com.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.InputStream;
import java.util.List;

import static com.backend.constants.AppConstant.*;

@RestController
@RequestMapping("/user/")
public class UserController {
    private Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;
    @Value("${user.image}")
    private String imageUploadPath;

    @PostMapping("/createUser")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        logger.info("Create  new User with userDto: {}", userDto);
        UserDto user = this.userService.createUser(userDto);
        logger.info("Successfully create  new User :{}", user);
        return new ResponseEntity<UserDto>(user, HttpStatus.CREATED);
    }

    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto, @PathVariable String userId) {
        logger.info("Update the User with userId and userDto :{}", userDto, userId);
        UserDto updateUser = this.userService.updateUser(userDto, userId);
        logger.info("Update User Successfully :{}", updateUser);
        return new ResponseEntity<UserDto>(updateUser, HttpStatus.OK);
    }

    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) throws Exception {
        logger.info("Fetch the single User with userID :{}", userId);
        UserDto userDto = this.userService.getUserById(userId);
        logger.info("Successfully Fetch the single User: {}", userDto);
        return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        logger.info("Delete the User with  userID :", userId);
        this.userService.deleteUser((userId));
        logger.info("Successfully Delete the User");
        return ResponseEntity.ok("Delete User Successfully");
    }

    @GetMapping("/getAllUser")
    public ResponseEntity<PagableResponce<UserDto>> getAllUser(
            @RequestParam(value = "pageNumber", defaultValue = PAGE_NUMBER, required = false) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = SORT_BY_NAME, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = SORT_DIR, required = false) String sortDir
    ) {
        logger.info("Fetching User by pagable parameter");
        logger.info("Page Number :{}", pageNumber);
        logger.info("Page Size:{}", pageSize);
        logger.info("Sort Direction :{}", sortDir);
        logger.info("Sort By :{}", sortBy);
        PagableResponce<UserDto> all = this.userService.getAll(pageNumber, pageSize, sortBy, sortDir);
        logger.info("Successfully Fetching the All Product :{}", all);

        return new ResponseEntity<PagableResponce<UserDto>>(all,HttpStatus.OK);
    }

    @GetMapping("/getUserByEmail/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        logger.info("Fetch the User with email :{}", email);
        UserDto user = this.userService.getUserByEmail(email);
        logger.info("Successfully Fetch the User :{}",user);
        return new ResponseEntity<UserDto>(user, HttpStatus.OK);
    }

    @GetMapping("/searchUser/{Keyword}")
    public ResponseEntity<List<UserDto>> searchUser(@PathVariable String Keyword) {
        logger.info("Fetch All User with Keyword :{}", Keyword);
        List<UserDto> list = userService.searchUser(Keyword);
        logger.info("Successfully Fetching  All User :{}",list);
        return new ResponseEntity<List<UserDto>>(list, HttpStatus.OK);
    }

    @PostMapping("/uploadImage/{userId}")
    public ResponseEntity<ImageResponce> uploadImage(@RequestParam("userImage") MultipartFile image, @PathVariable String userId) throws Exception {
        logger.info("Upload Image with userId :{}", userId);
        String imageName = this.fileService.uploadFile(image, imageUploadPath);
        UserDto user = this.userService.getUserById(userId);
        //  user.setUserId(imageName);
        user.setImageName(imageName);
        UserDto userDto = userService.updateUser(user, userId);
        logger.info("Successfully upload the image:{}", userDto.getImageName());
        ImageResponce imageResponce = ImageResponce.builder().imageName(imageName).success(true).status("Success").message("Upload Image Successfully").build();
        return new ResponseEntity<ImageResponce>(imageResponce, HttpStatus.CREATED);


    }

    // serve user Image
    @GetMapping("/image/{userId}")
    public ResponseEntity<?> serveUserImage(@PathVariable String userId, HttpServletResponse responce) throws Exception {
        logger.info(" Fetch Product Image with userId :{}", userId);
        UserDto user = userService.getUserById(userId);
        InputStream resource = fileService.getResource(imageUploadPath, user.getImageName());
        responce.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(resource, responce.getOutputStream());
        logger.info("Successfully Fetch the Image");
        return ResponseEntity.ok("Get User Image Successfully");
    }
}