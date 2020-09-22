/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.realtime;

import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class EventQueueDisplay extends JScrollPane {
	private static final long serialVersionUID = 1L;
	private final JList<Event> eventJList = new JList<>();
	private final EventListModel eventListModel = new EventListModel();

	public EventQueueDisplay() {
		super();
		getViewport().setView(this.eventJList);
		this.eventJList.setModel(this.eventListModel);
	}

	public void initialize(final EventQueue eQ) {
		this.eventListModel.setEventQueue(eQ);
	}

	private static class EventListModel extends AbstractListModel<Event> implements EventQueue.Listener {
		private static final long serialVersionUID = 1L;
		private EventQueue eventQueue;
		private List<Event> list;

		public EventListModel() {
			super();
		}

		public void setEventQueue(final EventQueue eQ) {
			if (this.eventQueue != null) {
				new Exception("going to setEventQueue!!").printStackTrace();
				this.list = this.eventQueue.getEventList();
				final int oldSize = this.list.size() - 1;
				if (oldSize >= 0) {
					SwingUtilities.invokeLater(() -> {
						try {
							fireIntervalRemoved(EventListModel.this, 0, oldSize);
						} catch (final Exception e) {
							System.out.println("error initializing elevator. Trying to continue");
							e.printStackTrace();
						}
					});
				}
			}
			this.eventQueue = eQ;
			this.eventQueue.addListener(this);
			this.list = this.eventQueue.getEventList();
			final int newSize = this.list.size() - 1;
			SwingUtilities.invokeLater(() -> fireIntervalAdded(EventListModel.this, 0, newSize));
		}

		@Override
		public Event getElementAt(final int index) {
			if (index >= this.list.size()) {
				return null;
			}
			return this.list.get(index);
		}

		@Override
		public int getSize() {
			if (this.list == null) {
				return 0;
			}
			final int size = this.list.size();
			return size;
		}

		@Override
		public void eventAdded(final Event e) {
			this.list = this.eventQueue.getEventList();
			final int index = this.list.indexOf(e);
			SwingUtilities.invokeLater(() -> fireIntervalAdded(EventListModel.this, index, index));
		}

		/**
		 * Event was removed. Tell Swing about it to remove it from the shown list.
		 */
		@Override
		public void eventRemoved(final Event e) {
			SwingUtilities.invokeLater(() -> {
				try {
					final int index = EventListModel.this.list.indexOf(e);
					if (index >= 0) {
						EventListModel.this.list = EventListModel.this.eventQueue.getEventList();
						fireIntervalRemoved(EventListModel.this, index, index);
					}
					// else the event was already removed. ignore. #1506
				} catch (final Exception e1) {
					e1.printStackTrace();
				}
			});
		}

		@Override
		public void eventError(final Exception ex) {
		}
	}
}
