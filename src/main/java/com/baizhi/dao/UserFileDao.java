package com.baizhi.dao;

import com.baizhi.entity.UserFile;

import java.util.List;

public interface UserFileDao {



    List<UserFile> findByUserId(Integer id);

    void save(UserFile userFile);

    UserFile findById(String id);

    void update(UserFile userFile);

    void delete(String id);
}
