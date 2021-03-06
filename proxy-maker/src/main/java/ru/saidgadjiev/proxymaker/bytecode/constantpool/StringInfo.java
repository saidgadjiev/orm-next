package ru.saidgadjiev.proxymaker.bytecode.constantpool;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This class represent string constant in constant pool.
 */
public class StringInfo extends ConstantInfo {

    /**
     * String constant tag.
     */
    private static final int TAG = 8;

    /**
     * String value index.
     */
    private int strIndex;

    /**
     * Create new instance which represent string constant in constant pool.
     * @param strIndex string value constant index
     * @param index this instance index in {@link ConstantPool}
     */
    public StringInfo(int strIndex, int index) {
        super(index);
        this.strIndex = strIndex;
    }

    @Override
    public int getTag() {
        return TAG;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StringInfo that = (StringInfo) o;

        return strIndex == that.strIndex;
    }

    @Override
    public int hashCode() {
        return strIndex;
    }

    @Override
    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(TAG);
        dataOutputStream.writeShort(strIndex);
    }
}
