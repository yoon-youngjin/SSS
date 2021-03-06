package dev.yoon.board.controller;

import dev.yoon.board.domain.File;
import dev.yoon.board.dto.FileDto;
import dev.yoon.board.dto.PostDto;
import dev.yoon.board.service.FileService;
import dev.yoon.board.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("post")
@RequiredArgsConstructor
public class PostRestController {

    private final PostService postService;
    private final FileService fileService;


    @PostMapping(value = "{boardId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPost(
            @PathVariable("boardId") Long id,
            @RequestPart(value = "file", required = false) List<MultipartFile> files,
            @RequestPart(value = "post") PostDto postDto,
            HttpServletRequest request) throws Exception {

        postDto.setBoardId(id);
//        log.info(request.getHeader("Content-Type"));
        this.postService.createPost(postDto, files);
    }

    @GetMapping()
    public List<PostDto> readPostAll() {
        log.info("in read post all");
        List<PostDto> postDtos = this.postService.readPostAll();
        return postDtos;
    }

    @GetMapping("{id}")
    public PostDto readPostOne(@PathVariable("id") Long id) {
        log.info("in read post one");
        return this.postService.readPostOne(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("{id}")
    public void updatePost(
            @PathVariable("id") Long id,
            @RequestPart(value = "file", required = false) List<MultipartFile> files,
            @RequestPart(value = "post") PostDto postDto) {
        log.info("target id: " + id);
        log.info("update content: " + postDto);

        List<File> fileList = fileService.findAllByPost(id);

        List<MultipartFile> multipartFileList = files;

        List<MultipartFile> addFileList = new ArrayList<>();

        // ?????? Post??? File ?????? ??????
        if (CollectionUtils.isEmpty(fileList)) {
            if (!CollectionUtils.isEmpty(multipartFileList)) {
                for (MultipartFile multipartFile : multipartFileList) {
                    addFileList.add(multipartFile);
                }
            }
        }
        // ?????? Post??? File ?????? ??????
        else {
            // ????????? ?????? ?????? ??????
            if (CollectionUtils.isEmpty(multipartFileList)) {
                for (File file : fileList)
                    fileService.deleteFile(file);
            }
            // ????????? ?????? ?????? ??????
            else {
                List<String> dbOriginNameList = new ArrayList<>();

                // DB??? ?????? ????????? ??????
                for (File file : fileList) {

                    FileDto fileDto = fileService.findByFileId(file.getId());

                    String dbOrigFileName = fileDto.getOrigFileName();

                    // ????????? ????????? ????????? ??? ??????????????? ????????? ?????? x ??????
                    if (!multipartFileList.contains(dbOrigFileName))
                        fileService.deleteFile(file);
                    else
                        // ????????? ?????? ????????? ??????
                        dbOriginNameList.add(dbOrigFileName);
                }

                for (MultipartFile multipartFile : multipartFileList) {

                    String multipartOrigName = multipartFile.getOriginalFilename();
                    // DB??? ?????? ??????
                    if (!dbOriginNameList.contains(multipartOrigName)) {
                        addFileList.add(multipartFile);
                    }
                }
            }


        }
        this.postService.updatePost(id, postDto, addFileList);

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("{id}")
    public void deletePost(@PathVariable("id") Long id, @RequestBody PostDto postDto) {
        this.postService.deletePost(id, postDto.getPw());
    }

}
