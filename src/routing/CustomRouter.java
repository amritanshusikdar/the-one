/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.Connection;
import core.Coord;
import core.Settings;

import java.util.Objects;

/**
 * Passive router that doesn't send anything unless commanded. This is useful
 * for external event -controlled routing or dummy nodes.
 * For implementation specifics, see MessageRouter class.
 */
public class CustomRouter extends MessageRouter {

	private double routerActiveTime = -1;

	private double trainProbability = 1;
	private double cpProbability = 0.1;
	private boolean toTrain = false;
	private boolean toCp = false;

	public CustomRouter(Settings s) {
		super(s);

		try {
			this.routerActiveTime = Double.parseDouble(s.getSetting("routerActiveTime"));
		} catch (Throwable ignored) {}
		try {
			this.trainProbability = Double.parseDouble(s.getSetting("trainProbability"));
		} catch (Throwable ignored) {}
		try {
			this.cpProbability = Double.parseDouble(s.getSetting("cpProbability"));
		} catch (Throwable ignored) {}
		//To do: set the buffer size to a random number between 0 and 3?
	}

	/**
	 * Copy-constructor.
	 * @param r Router to copy the settings from.
	 */
	protected CustomRouter(CustomRouter r) {
		super(r);
		this.routerActiveTime = r.routerActiveTime;

		this.trainProbability = r.trainProbability;
		this.cpProbability = r.cpProbability;
		this.toTrain = (Math.random() < this.trainProbability);
		this.toCp = (Math.random() < this.cpProbability * this.trainProbability);
	}

	@Override
	public void update() {
		super.update();
	}

	@Override
	public void changedConnection(Connection con) {
		if (this.routerActiveTime != -1) this.getHost().setRouterActiveTime(this.routerActiveTime);

		String groupId = con.getOtherNode(this.getHost()).groupId;

		if (con.isUp() && groupId.equals("train") && this.toTrain) {
			this.getHost().setTarget(new Coord(1135,120));
		}
		if (con.isUp() && groupId.equals("coffee") && this.toTrain && this.toCp) {
			this.getHost().setCheckpoint(new Coord(1000,50));
		}
	}

	@Override
	public MessageRouter replicate() {
		return new CustomRouter(this);
	}
}
