package ch.ethz.infk.dspa.helper.tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.java.tuple.Tuple7;

/**
 * Extension of tuple that allows to give elements a name
 */
public class NTuple7<T0, T1, T2, T3, T4, T5, T6> extends Tuple7<T0, T1, T2, T3, T4, T5, T6> {

	private static final long serialVersionUID = 1L;

	private Map<String, Integer> nameMap = new HashMap<>();

	public NTuple7() {
	}

	public NTuple7(String name0, T0 value0, String name1, T1 value1, String name2, T2 value2, String name3, T3 value3,
			String name4, T4 value4, String name5, T5 value5, String name6, T6 value6) {
		super(value0, value1, value2, value3, value4, value5, value6);

		this.nameMap.put(name0, 0);
		this.nameMap.put(name1, 1);
		this.nameMap.put(name2, 2);
		this.nameMap.put(name3, 3);
		this.nameMap.put(name4, 4);
		this.nameMap.put(name5, 5);
		this.nameMap.put(name6, 6);
	}

	public static <T0, T1, T2, T3, T4, T5, T6> NTuple7<T0, T1, T2, T3, T4, T5, T6> of(String name0, T0 value0,
			String name1,
			T1 value1, String name2, T2 value2, String name3, T3 value3, String name4, T4 value4, String name5,
			T5 value5, String name6, T6 value6) {
		return new NTuple7<>(name0, value0, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5,
				name6, value6);
	}

	public void setName(String name, int pos) {
		this.nameMap.put(name, pos);
	}

	public void setNames(List<String> names) {
		NTuple.setNames(names, getArity(), this.nameMap);
	}

	public <T> T get(String name) {
		NTuple.ensureContainsName(this.nameMap, name);
		return getField(this.nameMap.get(name));
	}

	public Map<String, Integer> getNameMap() {
		return this.nameMap;
	}

}
