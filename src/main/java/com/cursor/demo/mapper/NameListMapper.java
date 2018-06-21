package com.cursor.demo.mapper;


import com.cursor.demo.pojo.NameList;
import com.cursor.demo.pojo.NameListExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NameListMapper {
    int countByExample(NameListExample example);

    int deleteByExample(NameListExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NameList record);

    int insertSelective(NameList record);

    List<NameList> selectByExample(NameListExample example);

    NameList selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NameList record, @Param("example") NameListExample example);

    int updateByExample(@Param("record") NameList record, @Param("example") NameListExample example);

    int updateByPrimaryKeySelective(NameList record);

    int updateByPrimaryKey(NameList record);
}