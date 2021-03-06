package org.example.mapper;

import org.example.domain.BoardAttachVO;

import java.util.List;

public interface BoardAttachMapper {
    void insert(BoardAttachVO vo);
    void delete(String uuid);
    List<BoardAttachVO> findByBno(Long bno);
    void deleteAll(Long bno);
    List<BoardAttachVO> getOldFiles();

}
