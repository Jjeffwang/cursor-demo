package com.cursor.demo.cursor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cursor.demo.bo.LongCursor;
import com.cursor.demo.cursor.CursorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONReader;
import org.springframework.stereotype.Service;

import java.io.*;


@Service("fileCursor")
public class FileCursorManager implements CursorManager {

    Logger logger = LoggerFactory.getLogger(FileCursorManager.class);

    @Override
    public LongCursor read(String filePath) {
        LongCursor cursor;
        JSONReader reader = null;
        try {
            File file = new File(filePath);
            logger.info("read cursor from file={}" + file.getAbsolutePath());
            if (file.exists()) {
                reader = new JSONReader(new FileReader(file));
                cursor = reader.readObject(LongCursor.class);
                return cursor;
            }
        } catch (FileNotFoundException e) {
            logger.info("read cursor error" + e);

        } finally {
            if (reader != null)
                reader.close();
        }
        return null;
    }

    @Override
    public void write(String filePath, LongCursor cursor) throws IOException {

        File file = new File(filePath);
        if (cursor == null) {
            if (file.exists())
                file.delete();
        } else {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(filePath);
                JSON.writeJSONStringTo(cursor, fileWriter, new SerializerFeature[0]);
            } finally {
                if (fileWriter != null)
                    fileWriter.close();
            }

        }
    }
}
