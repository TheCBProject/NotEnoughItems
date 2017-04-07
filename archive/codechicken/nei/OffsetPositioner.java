package codechicken.nei;

import codechicken.nei.api.IStackPositioner;
import codechicken.nei.api.stack.PositionedStack;

import java.util.ArrayList;

public class OffsetPositioner implements IStackPositioner {

    public OffsetPositioner(int x, int y) {
        offsetX = x;
        offsetY = y;
    }

    @Override
    public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> ai) {
        for (PositionedStack stack : ai) {
            stack.relx += offsetX;
            stack.rely += offsetY;
        }
        return ai;
    }

    public int offsetX;
    public int offsetY;
}
