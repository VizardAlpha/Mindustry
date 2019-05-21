package io.anuke.mindustry.editor;

import io.anuke.annotations.Annotations.Struct;
import io.anuke.arc.collection.LongArray;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.gen.TileOp;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.Floor;

import static io.anuke.mindustry.Vars.content;
import static io.anuke.mindustry.Vars.world;

public class DrawOperation{
    private LongArray array = new LongArray();

    public boolean isEmpty(){
        return array.isEmpty();
    }

    public void addOperation(long op){
        array.add(op);
    }

    public void undo(MapEditor editor){
        for(int i = array.size - 1; i >= 0; i--){
            long l = array.get(i);
            array.set(i, TileOp.get(TileOp.x(l), TileOp.y(l), TileOp.type(l), get(editor.tile(TileOp.x(l), TileOp.y(l)), TileOp.type(l))));
            set(editor, editor.tile(TileOp.x(l), TileOp.y(l)), TileOp.type(l), TileOp.value(l));
        }
    }

    public void redo(MapEditor editor){
        for(int i = 0; i < array.size; i++){
            long l = array.get(i);
            array.set(i, TileOp.get(TileOp.x(l), TileOp.y(l), TileOp.type(l), get(editor.tile(TileOp.x(l), TileOp.y(l)), TileOp.type(l))));
            set(editor, editor.tile(TileOp.x(l), TileOp.y(l)), TileOp.type(l), TileOp.value(l));
        }
    }

    short get(Tile tile, byte type){
        if(type == OpType.floor.ordinal()){
            return tile.floorID();
        }else if(type == OpType.block.ordinal()){
            return tile.blockID();
        }else if(type == OpType.rotation.ordinal()){
            return tile.rotation();
        }else if(type == OpType.team.ordinal()){
            return tile.getTeamID();
        }else if(type == OpType.overlay.ordinal()){
            return tile.overlayID();
        }
        throw new IllegalArgumentException("Invalid type.");
    }

    void set(MapEditor editor, Tile tile, byte type, short to){
        editor.load(() -> {
            if(type == OpType.floor.ordinal()){
                tile.setFloor((Floor)content.block(to));
            }else if(type == OpType.block.ordinal()){
                Block block = content.block(to);
                world.setBlock(tile, block, tile.getTeam(), tile.rotation());
            }else if(type == OpType.rotation.ordinal()){
                tile.rotation(to);
            }else if(type == OpType.team.ordinal()){
                tile.setTeam(Team.all[to]);
            }else if(type == OpType.overlay.ordinal()){
                tile.setOverlayID(to);
            }
        });
        editor.renderer().updatePoint(tile.x, tile.y);
    }

    @Struct
    class TileOpStruct{
        short x;
        short y;
        byte type;
        short value;
    }

    public enum OpType{
        floor,
        block,
        rotation,
        team,
        overlay
    }
}
