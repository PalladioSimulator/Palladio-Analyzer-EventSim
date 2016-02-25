package edu.kit.ipd.sdq.eventsim.measurement;

public class MeasuringPointPair<F, S> extends MeasuringPoint<Pair<F, S>> {

	private MeasuringPoint<F> first;

	private MeasuringPoint<S> second;

	public MeasuringPointPair(MeasuringPoint<F> first, MeasuringPoint<S> second, String property, Object... contexts) {
		super(new Pair<>(first.getElement(), second.getElement()), property, contexts);
		this.first = first;
		this.second = second;
	}

	public MeasuringPoint<F> getFirst() {
		return first;
	}

	public MeasuringPoint<S> getSecond() {
		return second;
	}

}
