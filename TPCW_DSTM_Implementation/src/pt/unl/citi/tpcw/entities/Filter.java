package pt.unl.citi.tpcw.entities;

public interface Filter<T> {
	public boolean filter(T obj);
}
