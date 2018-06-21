package com.cursor.demo.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LongCursor implements Cursorable {
    private static final long serialVersionUID = 6165533674700331305L;
    private long from;
    private long to;
    private String biz;
    private Object extraInfo;

    public void setExtraInfo(Object var1) {
        this.extraInfo = var1;
    }

    public Object getExtraInfo() {
        return this.extraInfo;
    }

    public void setBiz(String var1) {
        this.biz = var1;
    }

    public String getBiz() {
        return this.biz;
    }

    public LongCursor() {
        this.from = 0L;
        this.to = 0L;
    }

    public LongCursor(String var1) {
        this.from = 0L;
        this.to = 0L;
        this.setBiz(var1);
    }

    public LongCursor(long var1, long var3) {
        this.from = 0L;
        this.to = 0L;
        this.from = var1;
        this.to = var3;
    }

    public LongCursor(long var1, long var3, String var5) {
        this(var1, var3);
        this.setBiz(var5);
    }

    public void moveFoward(long var1) {
        this.setFrom(this.to);
        this.setTo(this.to + var1);
    }

    public void moveFoward(long var1, long var3) {
        this.moveFoward(var1);
        if (this.getTo() > var3) {
            this.setTo(var3);
        }

    }

    public void moveBack(long var1) {
        this.setTo(this.from);
        this.setFrom(this.from - var1);
    }

    public String toString() {
        return "LongCursor [from=" + this.from + "(" + new Date(this.from) + "), to=" + this.to + "(" + new Date(this.to) + "), biz=" + this.biz + "]";
    }

    public List<Cursorable> divides(int var1) {
        ArrayList var2 = new ArrayList();
        if (var1 > 0) {
            long var3 = (this.to - this.from) / (long)var1;
            long var5 = this.from;
            LongCursor var7 = null;

            for(int var8 = 0; var8 < var1; ++var8) {
                var7 = new LongCursor(var5, var5 + var3, this.getBiz());
                var5 += var3;
                var2.add(var7);
            }

            if (var7 != null) {
                var7.setTo(this.to);
            }
        }

        return var2;
    }

    public int hashCode() {
        boolean var1 = true;
        byte var2 = 1;
        int var3 = 31 * var2 + (int)(this.from ^ this.from >>> 32);
        var3 = 31 * var3 + (int)(this.to ^ this.to >>> 32);
        return var3;
    }

    public Cursorable cloneObject() {
        return new LongCursor(this.from, this.to, this.biz);
    }

    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (var1 == null) {
            return false;
        } else if (this.getClass() != var1.getClass()) {
            return false;
        } else {
            LongCursor var2 = (LongCursor)var1;
            if (this.from != var2.from) {
                return false;
            } else {
                return this.to == var2.to;
            }
        }
    }

    public long getFrom() {
        return this.from;
    }

    public void setFrom(long var1) {
        this.from = var1;
    }

    public long getTo() {
        return this.to;
    }

    public void setTo(long var1) {
        this.to = var1;
    }
}
