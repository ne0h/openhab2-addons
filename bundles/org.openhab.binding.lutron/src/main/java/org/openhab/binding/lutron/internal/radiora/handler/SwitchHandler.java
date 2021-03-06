/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lutron.internal.radiora.handler;

import org.openhab.binding.lutron.internal.LutronBindingConstants;
import org.openhab.binding.lutron.internal.radiora.config.SwitchConfig;
import org.openhab.binding.lutron.internal.radiora.protocol.LocalZoneChangeFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.SetSwitchLevelCommand;
import org.openhab.binding.lutron.internal.radiora.protocol.ZoneMapFeedback;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RadioRA switches
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class SwitchHandler extends LutronHandler {

    private Logger logger = LoggerFactory.getLogger(SwitchHandler.class);

    public SwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (LutronBindingConstants.CHANNEL_SWITCH.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                SetSwitchLevelCommand cmd = new SetSwitchLevelCommand(getConfigAs(SwitchConfig.class).getZoneNumber(),
                        (OnOffType) command);

                getRS232Handler().sendCommand(cmd);
            }
        }
    }

    @Override
    public void handleFeedback(RadioRAFeedback feedback) {
        if (feedback instanceof LocalZoneChangeFeedback) {
            handleLocalZoneChangeFeedback((LocalZoneChangeFeedback) feedback);
        } else if (feedback instanceof ZoneMapFeedback) {
            handleZoneMapFeedback((ZoneMapFeedback) feedback);
        }
    }

    private void handleZoneMapFeedback(ZoneMapFeedback feedback) {
        char value = feedback.getZoneValue(getConfigAs(SwitchConfig.class).getZoneNumber());

        if (value == '1') {
            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.ON);
        } else if (value == '0') {
            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.OFF);
        }
    }

    private void handleLocalZoneChangeFeedback(LocalZoneChangeFeedback feedback) {
        if (feedback.getZoneNumber() == getConfigAs(SwitchConfig.class).getZoneNumber()) {
            if (LocalZoneChangeFeedback.State.CHG.equals(feedback.getState())) {
                logger.debug("Not Implemented Yet - CHG state received from Local Zone Change Feedback.");
            }

            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.valueOf(feedback.getState().toString()));
        }
    }
}
