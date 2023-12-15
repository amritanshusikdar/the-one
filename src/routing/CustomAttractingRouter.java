/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;

import java.util.List;

/**
 * Passive router that doesn't send anything unless commanded. This is useful
 * for external event -controlled routing or dummy nodes.
 * For implementation specifics, see MessageRouter class.
 */
public class CustomAttractingRouter extends MessageRouter {

	private double routerOffsetTime = 200;
	private double routerPeriod = 1000;
	int alreadySent = 0;
	private int sitPlacesRemaining;
	private int numberOfSits = 30;

	public CustomAttractingRouter(Settings s) {
		super(s);

		try {
			this.routerOffsetTime = Double.parseDouble(s.getSetting("routerOffsetTime"));
		} catch (Throwable ignored) {}
		try {
			this.routerPeriod = Double.parseDouble(s.getSetting("routerPeriod"));
		} catch (Throwable ignored) {}
		try {
			this.numberOfSits = (int) Double.parseDouble(s.getSetting("numberOfSits"));
		} catch (Throwable ignored) {}
	}

	/**
	 * Copy-constructor.
	 * @param r Router to copy the settings from.
	 */
	protected CustomAttractingRouter(CustomAttractingRouter r) {
		super(r);
		this.alreadySent = r.alreadySent;
		this.routerOffsetTime = r.routerOffsetTime;
		this.routerPeriod = r.routerPeriod;
		this.numberOfSits = r.numberOfSits;
	}

	@Override
	public void update() {
		super.update();

		if (SimClock.getTime() > this.alreadySent*this.routerPeriod + this.routerOffsetTime) {
			this.sitPlacesRemaining = numberOfSits;
			List<Connection> conns = this.getHost().getConnections();
			for (Connection c : conns) {
				DTNHost toHost = c.getOtherNode(this.getHost());
				String m = this.getHost().toString()+toHost.toString();
				addToMessages(new Message(this.getHost(), toHost, m, m.length()), true);
				sendMessage(m, toHost);
			}

			this.alreadySent++;
		}
	}

	@Override
	public void changedConnection(Connection con) {
		// -"-
	}

	@Override
	public MessageRouter replicate() {
		return new CustomAttractingRouter(this);
	}

	@Override
	public int receiveMessage (Message m, DTNHost from) {
		if (m.toString().equals("+1")) {
			if (this.sitPlacesRemaining <= 0) {
				String noPlacesMessage = "no places remaining";
				addToMessages(new Message(this.getHost(), from, noPlacesMessage, noPlacesMessage.length()), true);
				sendMessage(noPlacesMessage, from);
			}
			else {
				String noPlacesMessage = "places remaining";
				addToMessages(new Message(this.getHost(), from, noPlacesMessage, noPlacesMessage.length()), true);
				sendMessage(noPlacesMessage, from);
				this.sitPlacesRemaining--;
			}
		}
		return 0;
	}
}
