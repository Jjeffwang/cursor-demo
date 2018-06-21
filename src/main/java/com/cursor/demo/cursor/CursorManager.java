package com.cursor.demo.cursor;

import com.cursor.demo.bo.LongCursor;


import java.io.IOException;

public interface CursorManager {
    LongCursor read(String var1);

    void write(String var1, LongCursor var2) throws IOException;
}
