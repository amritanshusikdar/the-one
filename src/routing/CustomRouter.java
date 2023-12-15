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
	private DTNHost trainHost;
	private boolean presenceNotified = false;

	public CustomRouter(Settings s) {
		super(s);

		try {
			this.trainProbability = Double.parseDouble(s.getSetting("trainProbability"));
		} catch (Throwable ignored) {}
		try {
			this.cpProbability = Double.parseDouble(s.getSetting("cpProbability"));
		} catch (Throwable ignored) {}
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
		if (this.getHost().getIsArrived() && !this.presenceNotified && !this.getHost().getIsGone()) {
			String m = "+1";
			addToMessages(new Message(this.getHost(), this.trainHost, m, m.length()), true);
			sendMessage(m, this.trainHost);
			this.presenceNotified = true;
		}

		super.update();
	}

	@Override
	public void changedConnection(Connection con) {
	}

	@Override
	public int receiveMessage (Message m, DTNHost from) {
		String groupId = m.getFrom().groupId;
		if (groupId.equals("train") && !m.toString().equals("no places remaining") && !m.toString().equals("places remaining")) {
			if (this.trainCoords == null) {
				this.trainCoords = from.getLocation();
				this.trainHost = from;
			}
			this.toTrain = (Math.random() < this.trainProbability);

			if (this.toTrain) {
				this.getHost().setTargetCoords(trainCoords);
				this.presenceNotified = false;
			}
			if (this.toTrain && this.toCp) this.getHost().setCheckpoint(cpCoords); // sometimes the coffee shop sends its message first so we need to check it here as well
		}
		else if (groupId.equals("train") && m.toString().equals("no places remaining")) {
			this.getHost().setTrainFull(true);
			this.getHost().setTargetCoords(new Coord(0,0));
			this.getHost().setDestinationAlreadySet(false);
		}
		else if (groupId.equals("train") && m.toString().equals("places remaining")) {
			this.getHost().setTrainFull(false);
			this.getHost().setIsGone(true);
		}
		if (groupId.equals("coffee")) {
			if (cpCoords == null) cpCoords = from.getLocation();
			this.toCp = (Math.random() < this.cpProbability);

			if (this.toTrain && this.toCp) this.getHost().setCheckpoint(cpCoords);
		}

		return 0;
	}

	@Override
	public MessageRouter replicate() {
		return new CustomRouter(this);
	}
}
