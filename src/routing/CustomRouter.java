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
	public CustomRouter(Settings s) {
		super(s);
		try {
			this.routerActiveTime = Double.parseDouble(s.getSetting("routerActiveTime"));
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
	}

	@Override
	public void update() {
		super.update();
	}

	@Override
	public void changedConnection(Connection con) {
		// go to the train coordinates
		if (this.routerActiveTime != -1) this.getHost().setRouterActiveTime(this.routerActiveTime);
		String groupId = con.getOtherNode(this.getHost()).groupId;
		if (con.isUp()) {
			this.getHost().setNewDestination(new Coord(1135,120));
		}
	}

	@Override
	public MessageRouter replicate() {
		return new CustomRouter(this);
	}
}
