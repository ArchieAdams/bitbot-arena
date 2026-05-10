package uk.ac.york.bitbotarena;

public class BitBoard {
    private final int width;
    private final int height;
    private final long[] board;

    public BitBoard(int width, int height) {
        if (width > 64) {
            throw new IllegalArgumentException("Width cannot exceed 64 bits!");
        }
        this.width = width;
        this.height = height;

        this.board = new long[height];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setBit(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Coordinates out of bounds!");
        }
        board[y] |= (1L << x);
    }

    public boolean getBit(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Coordinates out of bounds!");
        }
        return (board[y] & (1L << x)) != 0;
    }

    public void setRow(long row, int index){
        this.board[index] = row;
    }

    public long getRow(int index){
        return board[index];
    }

    public long getRowMask(){
        return width == 64 ? -1L : (1L << width) - 1L;
    }

    public void clearBit(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Coordinates out of bounds!");
        }
        board[y] &= ~(1L << x);
    }

    public void clearBoard() {
        for (int i = 0; i < height; i++) {
            board[i] = 0L;
        }
    }
    
    public BitBoard copy(){
        BitBoard clone = new BitBoard(this.width,this.height);
        for (int i = 0; i < height; i++) {
            clone.setRow(this.board[i], i);
        }
        return clone;
    }

    public void and(BitBoard otherBoard){
        if (otherBoard.width != this.width || otherBoard.height != this.height){
            throw new IllegalArgumentException("Boards must be of the same size for operations!");
        }

        for (int i = 0; i < height; i++) {
            this.board[i] &= otherBoard.board[i];
        }
    }

    public void or(BitBoard otherBoard){
        if (otherBoard.width != this.width || otherBoard.height != this.height){
            throw new IllegalArgumentException("Boards must be of the same size for operations!");
        }
        for (int i = 0; i < height; i++) {
            this.board[i] |= otherBoard.board[i];
        }
    }

    public void xor(BitBoard otherBoard){
        if (otherBoard.width != this.width || otherBoard.height != this.height){
            throw new IllegalArgumentException("Boards must be of the same size for operations!");
        }
        for (int i = 0; i < height; i++) {
            this.board[i] ^= otherBoard.board[i];
        }
    }

    public void not(){
        long rowMask = this.getRowMask();
        for (int i = 0; i < height; i++) {
            this.board[i]= ~this.board[i] & rowMask;
        }
    }

    public void shiftLeft(int shift){
        long rowMask = this.getRowMask();
        for (int i = 0; i < height; i++) {
            this.board[i] = (this.board[i] << shift) & rowMask;
        }
    }

    public void shiftRight(int shift){
        for (int i = 0; i < height; i++) {
            this.board[i] >>>= shift;
        }
    }

    public void shiftUp(int shift){
        for (int i = 0; i < height - shift; i++) {
            this.board[i] = this.board[i + shift];
        }
        for (int i = height - shift; i < height; i++) {
            this.board[i] = 0L;
        }
    }
    
    public void shiftDown(int shift){
        for (int i = height - 1; i >= shift; i--) {
            this.board[i] = this.board[i - shift];
        }
        for (int i = 0; i < shift; i++) {
            this.board[i] = 0L;
        }
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                output.append(getBit(x, y) ? "1 " : "0 ");
            }
            output.append("\n");
        }
        return output.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BitBoard other = (BitBoard) obj;
        if (this.width != other.width || this.height != other.height) return false;
        for (int i = 0; i < height; i++) {
            if (this.board[i] != other.board[i]) return false;
        }
        return true;
    }
}