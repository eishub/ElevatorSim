/*
* Copyright 2004 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.sim.ui.multiple;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.Parameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ResultsSelection extends JPanel {
	private static final long serialVersionUID = 1L;
	private JComboBox<Parameter> secondaryChooser;
	private JComboBox<Parameter> primaryChooser;
	private JComboBox<StatisticVariable> statisticsChooser;
	private JComboBox<Parameter> averageChooser;
	private final List<StatisticVariable> statisticsVariables;
	private final List<ValueSelector> valueSelectors = new LinkedList<>();
	private final List<ResultsSelectionListener> listeners = new LinkedList<>();

	interface ResultsSelectionListener {
		void resultsSelected(MultipleValueParameter primaryVar, MultipleValueParameter secondaryVar,
				MultipleValueParameter averageVar, List<Parameter> otherVariables, StatisticVariable statistic);
	}

	void addResultsSelectionListener(final ResultsSelectionListener l) {
		this.listeners.add(l);
	}

	public ResultsSelection(final List<Parameter> rangeParams, final List<StatisticVariable> statisticsVariables,
			final ResultsSelectionListener l) {
		super();
		this.statisticsVariables = statisticsVariables;
		if (l != null) {
			addResultsSelectionListener(l);
		}

		setLayout(new BorderLayout());

		final Box selectionPanel = new Box(BoxLayout.Y_AXIS);

		final Box chooserPanel = new Box(BoxLayout.X_AXIS);
		final JComponent chooserBox = createChoosers(rangeParams);
		chooserPanel.add(chooserBox);

		// TODO: Add a color chooser for the graph
		final JComponent spinnerPanel = createSpinners(rangeParams);
		chooserPanel.add(spinnerPanel);
		selectionPanel.add(chooserPanel);
		updateValueSelectors();

		add(selectionPanel, BorderLayout.NORTH);
	}

	/**
	 * Notifies ResultsSelectionLister (which is the spinners on the right hand side
	 * of the UI) that the primary/secondary/average selections have changed.
	 */
	private void variablesUpdated() {
		final MultipleValueParameter primaryVar = (MultipleValueParameter) this.primaryChooser.getSelectedItem();
		final MultipleValueParameter secondaryVar = (MultipleValueParameter) this.secondaryChooser.getSelectedItem();
		final MultipleValueParameter averageVar = this.averageChooser.getSelectedItem() instanceof String ? null
				: (MultipleValueParameter) this.averageChooser.getSelectedItem();
		final List<Parameter> otherParameters = new LinkedList<>();
		for (final Object element : this.valueSelectors) {
			final ValueSelector vs = (ValueSelector) element;
			final boolean isPrimaryVariable = primaryVar != null
					&& vs.getParameter().getDescription().equals(primaryVar.getDescription());
			final boolean isSecondaryVariable = secondaryVar != null
					&& vs.getParameter().getDescription().equals(secondaryVar.getDescription());
			if (isPrimaryVariable || isSecondaryVariable) {
				continue;
			}
			otherParameters.add(vs.getSelectedParameter());
		}
		for (final Object element : this.listeners) {
			final ResultsSelectionListener l = (ResultsSelectionListener) element;
			l.resultsSelected(primaryVar, secondaryVar, averageVar, otherParameters,
					(StatisticVariable) this.statisticsChooser.getSelectedItem());
		}
	}

	private JComponent createSpinners(final List<Parameter> rangeParams) {
		// make spinners for all the parameters
		final JPanel spinnerPanel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;

		for (final Parameter parameter : rangeParams) {
			final MultipleValueParameter p = (MultipleValueParameter) parameter;
			final List<Parameter> paramList = p.getParameterList();
			final Map<Object, Parameter> paramMap = new HashMap<>();
			for (final Parameter parameter2 : paramList) {
				final SingleValueParameter param = (SingleValueParameter) parameter2;
				paramMap.put(param.getUIValue(), param);
			}
			final JLabel pName = new JLabel(p.getDescription());
			final ValueSelector pValue = new ValueSelector(p, paramList, paramMap, this::variablesUpdated);
			this.valueSelectors.add(pValue);
			gbc.gridx = 0;
			gbc.anchor = GridBagConstraints.WEST;
			spinnerPanel.add(pName, gbc);
			gbc.gridx = 1;
			gbc.anchor = GridBagConstraints.EAST;
			spinnerPanel.add(pValue, gbc);
			gbc.gridy++;
		}
		return spinnerPanel;
	}

	private JComponent createChoosers(final List<Parameter> rangeParamsInitial) {
		final List<Parameter> multiValueParams = new LinkedList<>();
		for (final Parameter parameter : rangeParamsInitial) {
			final MultipleValueParameter p = (MultipleValueParameter) parameter;
			if (p.isMultiple()) {
				multiValueParams.add(p);
			}
		}

		final JPanel chooserBox = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		final JLabel primaryLabel = new JLabel("Primary Variable");
		chooserBox.add(primaryLabel, gbc);
		final ComboBoxModel<Parameter> primaryComboBoxModel = new DefaultComboBoxModel<>(
				multiValueParams.toArray(new Parameter[multiValueParams.size()]));
		if (multiValueParams.size() > 0) {
			primaryComboBoxModel.setSelectedItem(multiValueParams.get(0));
		}

		this.primaryChooser = new JComboBox<>(primaryComboBoxModel);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		chooserBox.add(this.primaryChooser, gbc);
		if (multiValueParams.size() == 0) {
			this.primaryChooser.setVisible(false);
			primaryLabel.setVisible(false);
		}
		if (multiValueParams.size() == 1) {
			this.primaryChooser.setVisible(false);
			final String description = ((MultipleValueParameter) multiValueParams.get(0)).getDescription();
			chooserBox.add(new JLabel(description), gbc);
		}

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.WEST;
		final JLabel secondaryLabel = new JLabel("Secondary Variable");
		chooserBox.add(secondaryLabel, gbc);
		final SecondaryComboBoxModel secondaryComboBoxModel = new SecondaryComboBoxModel(multiValueParams,
				this.primaryChooser.getSelectedItem());
		this.secondaryChooser = new JComboBox<>(secondaryComboBoxModel);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		chooserBox.add(this.secondaryChooser, gbc);
		if (multiValueParams.size() < 2) {
			this.secondaryChooser.setVisible(false);
			secondaryLabel.setVisible(false);
		}

		// make stats chooser and button

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.WEST;
		chooserBox.add(new JLabel("Statistics Measurement"), gbc);
		this.statisticsChooser = new JComboBox<>(new ComboBoxModel<StatisticVariable>() {
			StatisticVariable selected = (ResultsSelection.this.statisticsVariables.size() > 0)
					? ResultsSelection.this.statisticsVariables.get(0)
					: null;

			@Override
			public StatisticVariable getSelectedItem() {
				return this.selected;
			}

			@Override
			public void setSelectedItem(final Object value) {
				this.selected = (StatisticVariable) value;
				variablesUpdated();
			}

			@Override
			public int getSize() {
				return ResultsSelection.this.statisticsVariables.size();
			}

			@Override
			public StatisticVariable getElementAt(final int index) {
				return ResultsSelection.this.statisticsVariables.get(index);
			}

			@Override
			public void addListDataListener(final ListDataListener arg0) {
			}

			@Override
			public void removeListDataListener(final ListDataListener arg0) {
			}
		});
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		chooserBox.add(this.statisticsChooser, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.WEST;
		chooserBox.add(new JLabel("Averaging"), gbc);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		final AverageComboBoxModel averageComboBoxModel = new AverageComboBoxModel(multiValueParams,
				(Parameter) this.primaryChooser.getSelectedItem(), (Parameter) this.secondaryChooser.getSelectedItem());
		this.averageChooser = new JComboBox<>(averageComboBoxModel);
		chooserBox.add(this.averageChooser, gbc);

		this.averageChooser.addItemListener(arg0 -> updateValueSelectors());

		this.secondaryChooser.addItemListener(arg0 -> {
			final MultipleValueParameter primary = (MultipleValueParameter) ResultsSelection.this.primaryChooser
					.getSelectedItem();
			final MultipleValueParameter secondary = (MultipleValueParameter) ResultsSelection.this.secondaryChooser
					.getSelectedItem();
			averageComboBoxModel.validateValues(primary, secondary);
			updateValueSelectors();
		});

		this.primaryChooser.addItemListener(arg0 -> {
			final MultipleValueParameter primary = (MultipleValueParameter) ResultsSelection.this.primaryChooser
					.getSelectedItem();
			final MultipleValueParameter secondary = (MultipleValueParameter) ResultsSelection.this.secondaryChooser
					.getSelectedItem();
			secondaryComboBoxModel.validateValues(primary);
			averageComboBoxModel.validateValues(primary, secondary);
			updateValueSelectors();
		});

		return chooserBox;
	}

	private void updateValueSelectors() {
		final MultipleValueParameter primary = (MultipleValueParameter) this.primaryChooser.getSelectedItem();
		final MultipleValueParameter secondary = (MultipleValueParameter) this.secondaryChooser.getSelectedItem();
		final MultipleValueParameter average = this.averageChooser.getSelectedItem() instanceof String ? null
				: (MultipleValueParameter) this.averageChooser.getSelectedItem();
		for (final Object element : this.valueSelectors) {
			final ValueSelector selector = (ValueSelector) element;
			if (selector.getParameter() == primary) {
				selector.setPrimary();
			} else if (selector.getParameter() == secondary) {
				selector.setSecondary();
			} else if (selector.getParameter() == average) {
				selector.setAverage();
			} else {
				selector.setTertiary();
			}
		}
		variablesUpdated();
	}

	/**
	 * @param spinnerValues        Maps Parameter to JSpinner
	 * @param parameterDescription The description for the parameter
	 * @return the value of the spinner if the parameterDescription matches, else
	 *         null meaning the parameterDescription does not match.
	 */
	static String getSpinnerValue(final Map<Parameter, JSpinner> spinnerValues, final String parameterDescription) {
		for (final Entry<Parameter, JSpinner> entry : spinnerValues.entrySet()) {
			final SingleValueParameter param = (SingleValueParameter) entry.getKey();
			if (parameterDescription.equals(param.getDescription())) {
				final JSpinner spinner = entry.getValue();
				return (String) spinner.getValue();
			}
		}
		return null;
	}

	private class AverageComboBoxModel implements ComboBoxModel<Parameter> {
		private final List<Parameter> rangeParams;
		private final List<Parameter> currentParams = new LinkedList<>();
		private Parameter selected;
		private final List<ListDataListener> listeners = new LinkedList<>();

		// TODO: handle the case where no secondary var is selected
		public AverageComboBoxModel(final List<Parameter> params, final Parameter primarySelected,
				final Parameter secondarySelected) {
			super();
			this.rangeParams = params;
			updateCurrentList(primarySelected, secondarySelected);
		}

		private void updateCurrentList(final Parameter primarySelected, final Parameter secondarySelected) {
			this.currentParams.clear();
			for (final Parameter o : this.rangeParams) {
				if (!o.equals(primarySelected) && !o.equals(secondarySelected)) {
					this.currentParams.add(o);
				}
			}
		}

		@Override
		public Parameter getSelectedItem() {
			return this.selected;
		}

		@Override
		public void setSelectedItem(final Object index) {
			this.selected = (Parameter) index;
			// At least the secondary (and maybe the primary) has changed; update the
			// selectors
		}

		@Override
		public int getSize() {
			return this.currentParams.size() + 1;
		}

		@Override
		public Parameter getElementAt(final int index) {
			return this.currentParams.get(index - 1);
		}

		@Override
		public void addListDataListener(final ListDataListener l) {
			this.listeners.add(l);
		}

		@Override
		public void removeListDataListener(final ListDataListener l) {
			this.listeners.remove(l);
		}

		public void validateValues(final MultipleValueParameter primary, final MultipleValueParameter secondary) {
			updateCurrentList(primary, secondary);
			// if the current selection is no longer in the list, default to the first
			if (!this.currentParams.contains(this.selected)) {
				ResultsSelection.this.averageChooser.setSelectedIndex(0);
			}
			for (final ListDataListener listDataListener : this.listeners) {
				final ListDataListener l = listDataListener;
				l.contentsChanged(
						new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.currentParams.size()));
			}
		}
	}

	private class SecondaryComboBoxModel implements ComboBoxModel<Parameter> {
		private final List<Parameter> rangeParams;
		private final List<Parameter> currentParams = new LinkedList<>();
		private Parameter selected;
		private final List<ListDataListener> listeners = new LinkedList<>();

		// TODO: handle the case where no secondary var is selected
		public SecondaryComboBoxModel(final List<Parameter> params, final Object primarySelected) {
			super();
			this.rangeParams = params;
			updateCurrentList(primarySelected);
			if (this.currentParams.size() > 0) {
				this.selected = this.currentParams.get(0);
			}
		}

		private void updateCurrentList(final Object primarySelected) {
			this.currentParams.clear();
			for (final Parameter o : this.rangeParams) {
				if (!o.equals(primarySelected)) {
					this.currentParams.add(o);
				}
			}
		}

		@Override
		public Parameter getSelectedItem() {
			return this.selected;
		}

		@Override
		public void setSelectedItem(final Object index) {
			this.selected = (Parameter) index;
			// At least the secondary (and maybe the primary) has changed; update the
			// selectors
			updateValueSelectors();
		}

		@Override
		public int getSize() {
			return this.currentParams.size();
		}

		@Override
		public Parameter getElementAt(final int index) {
			return this.currentParams.get(index);
		}

		@Override
		public void addListDataListener(final ListDataListener l) {
			this.listeners.add(l);
		}

		@Override
		public void removeListDataListener(final ListDataListener l) {
			this.listeners.remove(l);
		}

		public void validateValues(final MultipleValueParameter primaryParameter) {
			if (this.currentParams.contains(primaryParameter)) {
				updateCurrentList(primaryParameter);
				for (final Object element : this.listeners) {
					final ListDataListener l = (ListDataListener) element;
					l.contentsChanged(
							new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.currentParams.size()));
				}
				if (!this.currentParams.contains(this.selected)) {
					ResultsSelection.this.secondaryChooser.setSelectedIndex(0);
				}
			}
		}
	}
}
