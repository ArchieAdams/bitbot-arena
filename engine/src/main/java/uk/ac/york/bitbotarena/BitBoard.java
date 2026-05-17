package uk.ac.york.bitbotarena;

public class BitBoard {
    private final int width;
    private final int height;
    private final long[] board;

    public BitBoard(int width, int height) {
        if (width > 64) {
            throw new IllegalArgumentException("Width cannot exceed 64 bits!");
        }
        if (height > 64) {
            throw new IllegalArgumentException("Height cannot exceed 64 bits!");
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
        if (index < 0 || index >= height) {
            throw new IndexOutOfBoundsException("Index out of bounds!");
        }
        this.board[index] = row & this.getRowMask();
    }

    public long getRow(int index){
        if (index < 0 || index >= height) {
            throw new IndexOutOfBoundsException("Index out of bounds!");
        }
        return board[index];
    }

    public void setColumn(long column, int index){
        if (index < 0 || index >= width) {
            throw new IndexOutOfBoundsException("Index out of bounds!");
        }
        for (int i = 0; i < this.height; i++) {
            boolean currentBit = (column & (1L << i))!=0;
            if (currentBit) {
                this.setBit(index, i);
            }
            else {
                this.clearBit(index, i);
            }
        }
    }

    public long getColumn(int index){
        if (index < 0 || index >= width) {
            throw new IndexOutOfBoundsException("Index out of bounds!");
        }
        long column = 0L;
        for (int i = 0; i < this.height; i++) {
            if (this.getBit(index, i)) {
                column |= (1L << i);
            }
        }
        return column;
    }

    public boolean isEmpty(){
        long total=0L;
        for (int i = 0; i < this.height; i++) {
            total |= this.board[i];
        }
        return total == 0L;
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

    public BitBoard andOutput(BitBoard otherBoard){
        BitBoard clone = this.copy();
        clone.and(otherBoard);
        return clone;
    }

    public void or(BitBoard otherBoard){
        if (otherBoard.width != this.width || otherBoard.height != this.height){
            throw new IllegalArgumentException("Boards must be of the same size for operations!");
        }
        for (int i = 0; i < height; i++) {
            this.board[i] |= otherBoard.board[i];
        }
    }

    public BitBoard orOutput(BitBoard otherBoard){
        BitBoard clone = this.copy();
        clone.or(otherBoard);
        return clone;
    }

    public void xor(BitBoard otherBoard){
        if (otherBoard.width != this.width || otherBoard.height != this.height){
            throw new IllegalArgumentException("Boards must be of the same size for operations!");
        }
        for (int i = 0; i < height; i++) {
            this.board[i] ^= otherBoard.board[i];
        }
    }

    public BitBoard xorOutput(BitBoard otherBoard){
        BitBoard clone = this.copy();
        clone.xor(otherBoard);
        return clone;
    }

    public void not(){
        long rowMask = this.getRowMask();
        for (int i = 0; i < height; i++) {
            this.board[i]= ~this.board[i] & rowMask;
        }
    }

    public BitBoard notOutput(){
        BitBoard clone = this.copy();
        clone.not();
        return clone;
    }

    public void shiftEast(int shift){
        long rowMask = this.getRowMask();
        for (int i = 0; i < height; i++) {
            this.board[i] = (this.board[i] << shift) & rowMask;
        }
    }

    public BitBoard shiftEastOutput(int shift){
        BitBoard clone = this.copy();
        clone.shiftEast(shift);
        return clone;
    }

    public void shiftWest(int shift){
        for (int i = 0; i < height; i++) {
            this.board[i] >>>= shift;
        }
    }

    public BitBoard shiftWestOutput(int shift){
        BitBoard clone = this.copy();
        clone.shiftWest(shift);
        return clone;
    }

    public void shiftNorth(int shift){
        for (int i = 0; i < height - shift; i++) {
            this.board[i] = this.board[i + shift];
        }
        for (int i = height - shift; i < height; i++) {
            this.board[i] = 0L;
        }
    }

    public BitBoard shiftNorthOutput(int shift){
        BitBoard clone = this.copy();
        clone.shiftNorth(shift);
        return clone;
    }
    
    public void shiftSouth(int shift){
        for (int i = height - 1; i >= shift; i--) {
            this.board[i] = this.board[i - shift];
        }
        for (int i = 0; i < shift; i++) {
            this.board[i] = 0L;
        }
    }

    public BitBoard shiftSouthOutput(int shift){
        BitBoard clone = this.copy();
        clone.shiftSouth(shift);
        return clone;
    }

    public void shift(Movement movement,int shift){
        switch (movement) {
            case NORTH -> this.shiftNorth(shift);
            case EAST -> this.shiftEast(shift);
            case SOUTH -> this.shiftSouth(shift);
            case WEST -> this.shiftWest(shift);
        }
    }

    public BitBoard shiftOutput(Movement movement,int shift){
        BitBoard clone = this.copy();
        clone.shift(movement,shift);
        return clone;
    }

    public int getWeight() {
        int total = 0;

        for (int y = 0; y < height; y++) {
            total += Long.bitCount(this.board[y]);
        }

        return total;
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