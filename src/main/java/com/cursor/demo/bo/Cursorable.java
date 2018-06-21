package com.cursor.demo.bo;

import java.io.Serializable;
import java.util.List;

public interface Cursorable extends Serializable {

    List<Cursorable> divides(int var1);

    Cursorable cloneObject();
}
