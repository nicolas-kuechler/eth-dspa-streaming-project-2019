package ch.ethz.infk.dspa.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.test.util.AbstractTestBase;
import org.junit.jupiter.api.Test;

import ch.ethz.infk.dspa.avro.Comment;
import ch.ethz.infk.dspa.avro.CommentPostMapping;
import ch.ethz.infk.dspa.stream.helper.SourceSink;
import ch.ethz.infk.dspa.stream.helper.TestSink;
import ch.ethz.infk.dspa.stream.testdata.CommentTestDataGenerator;

public class CommentStreamBuilderWithPostIdTest extends AbstractTestBase {

	@Test
	public void testBase() throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		String testFile = "./../data/test/01_test/comment_event_stream.csv";
		Time maxOutOfOrderness = Time.hours(1);

		// all replies will produce a mapping
		Long mappingCount = new CommentTestDataGenerator().generate(testFile).stream()
				.filter(c -> c.getReplyToCommentId() != null).count();

		// create a SourceSink that acts both as Sink and Source for the
		// CommentPostMappings (instead of going via Kafka)
		SourceSink mappingSourceSink = new SourceSink(mappingCount);

		// create artificial streams
		DataStream<Comment> commentStream = new CommentTestDataGenerator().generate(env, testFile, maxOutOfOrderness);
		DataStream<CommentPostMapping> mappingStream = env.addSource(mappingSourceSink);

		// build the enriched comment stream
		DataStream<Comment> enrichedCommentStream = new CommentDataStreamBuilder(env)
				.withCommentStream(commentStream)
				.withPostIdEnriched()
				.withCommentPostMappingStream(mappingStream)
				.withCommentPostMappingSink(mappingSourceSink)
				.withMaxOutOfOrderness(maxOutOfOrderness)
				.build();

		// collect the results in the collect sink where they can be checked
		enrichedCommentStream.addSink(new TestSink<Comment>());
		env.execute();

		List<Comment> results = TestSink.getResults(Comment.class);

		Map<Long, Long> map = getCommentToPostMapping(testFile);

		assertEquals(map.size(), results.size(), "Event Count");

		for (Comment comment : results) {
			Long commentId = comment.getId();
			Long postId = comment.getReplyToPostId();
			assertEquals(map.get(commentId), postId, "Comment " + commentId);
		}
	}

	private Map<Long, Long> getCommentToPostMapping(String file) throws IOException {

		Map<Long, Long> map = new HashMap<>();
		List<Comment> comments = new CommentTestDataGenerator().generate(file);

		// sort such that order is in event time
		Collections.sort(comments, new Comparator<Comment>() {
			@Override
			public int compare(Comment o1, Comment o2) {
				return o1.getCreationDate().compareTo(o2.getCreationDate());
			}
		});

		for (Comment comment : comments) {
			Long commentId = comment.getId();
			Long postId;
			if (comment.getReplyToPostId() != null) {
				postId = comment.getReplyToPostId();
			} else if (map.get(comment.getReplyToCommentId()) != null) {
				postId = map.get(comment.getReplyToCommentId());
			} else {
				throw new IllegalArgumentException("The given file does not represent a well formed comment stream");
			}
			map.put(commentId, postId);
		}

		return map;
	}

}
