package s0572411;

import java.awt.Color;
import java.awt.Point;
import java.io.Console;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

public class W1 extends AI {

	public W1(Info info) {
		super(info);
		enlistForTournament(572411);
	}

	@Override
	public String getName() {
		return "AI Taucher";
		
	}

	@Override
	public Color getPrimaryColor() {
		return Color.black;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.white;
	}

	@Override
	public PlayerAction update() {
		return new DivingAction(1.0f,3.14f);
	}

	//
	public PlayerAction setGoal() {
		
		return null;
	}
	
	public DivingAction persueClosestPearl(Point goal) {
		Point playerPos = new Point();
		playerPos.x = (int)info.getX();
		playerPos.y = (int)info.getY();
		return null;
	}
	
	public void printSceneInfo() {
		System.out.print("thing");
		System.out.print(info);
	}
}
