package ch.ethz.infk.dspa.stream.testdata;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.joda.time.DateTime;
import org.json.JSONObject;

import ch.ethz.infk.dspa.recommendations.dto.PersonActivity;
import ch.ethz.infk.dspa.recommendations.dto.PersonActivity.PersonActivityType;

public class PersonActivityTestDataGenerator
		extends AbstractTestDataGenerator<PersonActivity> {

	@Override
	public DataStream<PersonActivity> addReturnType(SingleOutputStreamOperator<PersonActivity> out) {
		return out.returns(PersonActivity.class);
	}

	@Override
	public AssignerWithPeriodicWatermarks<TestDataPair<PersonActivity>> getTimestampsAndWatermarkAssigner(
			Time maxOutOfOrderness) {
		return new BoundedOutOfOrdernessTimestampExtractor<TestDataPair<PersonActivity>>(maxOutOfOrderness) {
			private static final long serialVersionUID = 1L;

			@Override
			public long extractTimestamp(TestDataPair<PersonActivity> pair) {
				return pair.timestamp.getMillis();
			}
		};
	}

	@Override
	public PersonActivity generateElement() {
		PersonActivity activity = new PersonActivity();
		activity.setPersonId(1l);
		activity.setPostId(2l);
		activity.setType(PersonActivityType.POST);

		HashMap<String, Integer> categoryMap = new HashMap<>();
		categoryMap.put("c1", 1);
		categoryMap.put("c2", 2);
		activity.setCategoryMap(categoryMap);

		return activity;
	}

	@Override
	public TestDataPair<PersonActivity> parseLine(String line) {
		String[] parts = line.split("\\|");

		Long personId = Long.parseLong(parts[0]);

		Long postId = null;
		if (StringUtils.isNotEmpty(parts[1])) {
			postId = Long.parseLong(parts[1]);
		}

		DateTime creationDate = parseDateTime(parts[2]);

		PersonActivityType type = null;
		if (StringUtils.isNotEmpty(parts[3])) {
			type = PersonActivityType.valueOf(parts[3]);
		}

		HashMap<String, Integer> categoryMap = new HashMap<>();
		if (parts.length > 4 && StringUtils.isNotEmpty(parts[4])) {
			JSONObject jsonObj = new JSONObject(parts[4]);
			jsonObj.toMap().entrySet().forEach(e -> categoryMap.put(e.getKey(), (Integer) e.getValue()));
		}

		PersonActivity activity = new PersonActivity();
		activity.setPersonId(personId);
		activity.setPostId(postId);
		activity.setType(type);
		activity.setCategoryMap(categoryMap);

		return TestDataPair.of(activity, creationDate);
	}

}
