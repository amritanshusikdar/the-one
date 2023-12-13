/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;

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

	private Coord trainCoords = null;
	private Coord cpCoords = null;

	public CustomRouter(Settings s) {
		super(s);

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
		this.trainCoords = r.trainCoords;
		this.cpCoords = r.cpCoords;
	}

	@Override
	public void update() {
		super.update();
	}

	@Override
	public void changedConnection(Connection con) {
	}

	public int receiveMessage (Message m, DTNHost from) {
		String groupId = m.getFrom().groupId;
		if (groupId.equals("train")) {
			if (trainCoords == null) trainCoords = m.getFrom().getLocation();
			this.toTrain = (Math.random() < this.trainProbability);

			if (this.toTrain) this.getHost().setTarget(trainCoords);
			if (this.toTrain && this.toCp) this.getHost().setCheckpoint(cpCoords); // sometimes the coffee shop sends its message first so we need to check it here as well
		}
		if (groupId.equals("coffee")) {
			if (cpCoords == null) cpCoords = m.getFrom().getLocation();
			this.toCp = (Math.random() < this.cpProbability * this.trainProbability);

			if (this.toTrain && this.toCp) this.getHost().setCheckpoint(cpCoords);
		}

		return 0;
	}

	@Override
	public MessageRouter replicate() {
		return new CustomRouter(this);
	}
}
