package com.github.DenFade.Saturn.entity.game;

import com.github.DenFade.Saturn.entity.IEntity;
import com.github.DenFade.Saturn.util.EmojiFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class MineSweeper extends IEntity {

    private final int x;
    private final int y;
    private final int rate;
    private final int b;
    private final int size;
    private boolean die = false;
    private final Cell[][] table;
    private static final int BOMB = 9;
    private static final int[][] around = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
    private static final String[] emoji = {EmojiFactory.BLUE_SQUARE.getEmoji(), ":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:",":eight:", EmojiFactory.BOMB.getEmoji(), EmojiFactory.TRIANGULAR_FLAG_ON_POST.getEmoji(), EmojiFactory.WHITE_LARGE_SQUARE.getEmoji(), EmojiFactory.COLLISION.getEmoji(), ":x:"};

    private final String ownerId;
    private String messageId;

    private final long startAt;
    private int leftTile;
    private int leftBomb;
    private final List<String> participants = new ArrayList<>();

    public MineSweeper(int x, int y, int rate, String ownerId){
        this.x = x;
        this.y = y;
        this.rate = rate;
        this.b = (x*y*(rate+10))/100+1;
        this.size = x*y;
        this.table = new Cell[y][x];
        this.ownerId = ownerId;
        for(int i = 0; i < this.y; i++){
            for(int k = 0; k < this.x; k++){
                this.table[i][k] = new Cell(0, false, false);
            }
        }
        this.startAt = System.currentTimeMillis();
        this.leftTile = this.size - this.b;
        this.leftBomb = this.b;
        this.participants.add(ownerId);
    }

    public boolean isSafe(int x, int y){
        return x >= 0 && x < this.x && y >= 0 && y < this.y;
    }

    protected int[] splitC(int xy){
        return (new int[]{xy%this.x, xy/this.x});
    }

    protected int sumC(int x, int y){
        return x+(y*this.x);
    }

    protected void access3x3(int x, int y, BiConsumer<Integer, Integer> task){
        for(int[] r : around){
            int nx = x + r[0];
            int ny = y + r[1];
            if(isSafe(nx, ny)) task.accept(nx, ny);
        }
    }

    protected int access3x3s(int x, int y, BiPredicate<Integer, Integer> task){
        int s = 0;
        for(int[] r : around){
            int nx = x + r[0];
            int ny = y + r[1];
            if(isSafe(nx, ny)) {
                if(task.test(nx, ny)) s++;
            }
        }
        return s;
    }

    public void setMine(){
        List<Integer> arr = new ArrayList<>();
        List<Integer> bombs;
        for(int i = 0; i < this.size; i++) arr.add(i);
        Collections.shuffle(arr);

        bombs = arr.subList(0, this.b);
        for(int c : bombs){
            int[] r = splitC(c);
            this.table[r[1]][r[0]].tile = BOMB;
            access3x3(r[0], r[1], (x,y) -> {
                if(this.table[y][x].tile != BOMB) this.table[y][x].tile += 1;
            });
        }
    }

    public Display open(int x, int y, boolean ignore){
        if(!isSafe(x, y)) return Display.ONGOING;

        Cell current = this.table[y][x];
        if(current.opened){
            if(ignore || current.tile == 0) return Display.ONGOING;
            else if(0 < current.tile && current.tile < 9){
                int flags = this.access3x3s(x, y, (nx, ny) -> {
                    Cell current2 = this.table[ny][nx];
                    return current2.flag && !current2.opened;
                });
                if(flags == current.tile){
                    this.table[y][x].opened = true;
                    access3x3(x, y, (cx, cy) -> open(cx, cy, true));
                    return Display.ONGOING;
                }
            }
            return Display.ONGOING;
        } else {
            if(current.tile == BOMB){
                if(current.flag) return Display.ONGOING;
                this.die = true;
                return Display.DIED;
            } else if(current.tile == 0){
                leftTile--;
                this.table[y][x].opened = true;
                access3x3(x, y, (cx, cy) -> open(cx, cy, true));
            } else {
                leftTile--;
                this.table[y][x].opened = true;
            }
            return leftTile == 0 ? Display.COMPLETED : Display.ONGOING;
        }
    }

    public void flagging(int x, int y){
        if(!isSafe(x, y)) return;
        else {
            Cell cur = this.table[y][x];
            if(!cur.opened){
                boolean f = cur.flag;
                if(f){
                    this.table[y][x].flag = false;
                    leftBomb++;
                } else {
                    this.table[y][x].flag = true;
                    leftBomb--;
                }

            }
        }
    }

    public String display(Display mode, int x, int y){
        StringBuilder dis = new StringBuilder();
        for (int i = 0; i < this.y; i++) {
            dis.append("\n");
            for (int j = 0; j < this.x; j++) {
                Cell current = this.table[i][j];
                switch (mode){
                    case ONGOING:
                        if(current.opened) dis.append(emoji[current.tile]);
                        else {
                            if(current.flag) dis.append(emoji[10]);
                            else dis.append(emoji[11]);
                        }
                        break;
                    case DIED:
                        if(current.tile != BOMB && current.flag) dis.append(emoji[13]);
                        else if(current.tile == BOMB && i == y && j == x) dis.append(emoji[12]);
                        else {
                            if(current.flag) dis.append(emoji[10]);
                            else dis.append(emoji[current.tile]);
                        }
                        break;
                    case COMPLETED:
                        dis.append(current.tile == BOMB ? emoji[10] : emoji[current.tile]);
                        break;
                }
            }
        }
        return dis.toString();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getBomb() {
        return b;
    }

    public int getRate() {
        return rate;
    }

    public long getStartAt() {
        return startAt;
    }

    public int getLeftBomb() {
        return leftBomb;
    }

    public boolean isDie() {
        return die;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void addParticipants(String id){
        participants.add(id);
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    private static class Cell{

        int tile;
        boolean flag;
        boolean opened;

        Cell(int t,boolean f,boolean o){
            this.tile = t;
            this.flag = f;
            this.opened = o;
        }

        public int get(){
            return this.tile;
        }

        public boolean isFlag(){
            return this.flag;
        }

        public boolean isOpened(){
            return this.opened;
        }
    }

    @Override
    public String toString() {
        StringBuilder t = new StringBuilder();
        for(int i = 0; i < this.y; i++){
            for(int k = 0; k < this.x; k++){
                t.append(this.table[i][k].tile);
            }
            t.append("\n");
        }
        return "-- "+this.x+"x"+this.y+"/"+this.b+" Game--\n\n"+t;

    }

    public enum Display{
        ONGOING, DIED, COMPLETED;

        @Override
        public String toString() {
            return this.name();
        }
    }
}
