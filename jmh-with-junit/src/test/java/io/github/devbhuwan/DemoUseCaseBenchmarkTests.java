package io.github.devbhuwan;


import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static io.github.devbhuwan.DemoUseCaseBenchmarkTests.BenchmarkState.AVERAGE_EXPECTED_TIME;

public class DemoUseCaseBenchmarkTests {

//    @ClassRule
//    public static RuleChain chain = RuleChain
//        .outerRule(new JmhCleaner(DemoUseCaseBenchmarkTests.class))
//        .around(JavaAgentSkip.ifPresent());

    @Test
    public void givenInputWhenDoJobThenBenchmarkNotExceedToAverageExpectedTime() throws RunnerException {
        Options opt = initBench();
        Collection<RunResult> results = runBench(opt);
        assertOutputs(results);
    }

    @Benchmark
    public void doJob(BenchmarkState benchmarkState) {
        benchmarkState.useCase.doJob();
    }

    private Options initBench() {
        return new OptionsBuilder()
            .include(DemoUseCase.class.getSimpleName() + ".*")
            .mode(Mode.AverageTime)
            .verbosity(VerboseMode.EXTRA)
            .timeUnit(TimeUnit.MILLISECONDS)
            .warmupTime(TimeValue.seconds(1))
            .measurementTime(TimeValue.milliseconds(1))
            .measurementIterations(2)
            .threads(4)
            .warmupIterations(2)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .forks(1)
            .build();
    }

    private Collection<RunResult> runBench(Options opt) throws RunnerException {
        return new Runner(opt).run();
    }

    private void assertOutputs(Collection<RunResult> results) {
        for (RunResult r : results) {
            for (BenchmarkResult rr : r.getBenchmarkResults()) {

                Mode mode = rr.getParams().getMode();
                double score = rr.getPrimaryResult().getScore();
                String methodName = rr.getPrimaryResult().getLabel();

                Assert.assertEquals("Test mode is not average mode. Method = " + methodName,
                    Mode.AverageTime, mode);
                Assert.assertTrue("Benchmark score = " + score + " is higher than " + AVERAGE_EXPECTED_TIME + " " + rr.getScoreUnit() + ". Too slow performance !",
                    score < AVERAGE_EXPECTED_TIME);
            }
        }
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        static final double AVERAGE_EXPECTED_TIME = 100;
        private DemoUseCase useCase = new DemoUseCase();
    }
}
