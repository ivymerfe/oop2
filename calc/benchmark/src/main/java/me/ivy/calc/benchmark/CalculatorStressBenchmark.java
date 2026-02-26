package me.ivy.calc.benchmark;

import me.ivy.calc.Calculator;
import me.ivy.calc.CommandException;
import me.ivy.calc.CommandFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@Fork(1)
@State(Scope.Benchmark)
public class CalculatorStressBenchmark {

    @Param({"10000", "50000", "100000"})
    public int operations;

    private String stressProgram;

    @Setup(org.openjdk.jmh.annotations.Level.Trial)
    public void setup() {
    }

    @Setup(org.openjdk.jmh.annotations.Level.Iteration)
    public void setupIteration() {
        stressProgram = buildStressProgram(operations);
    }

    @Benchmark
    public void heavyBatchExecution() throws CommandException {
        Calculator calculator = createCalculatorWithCommands();
        calculator.execute(stressProgram);
    }

    private static Calculator createCalculatorWithCommands() {
        return new Calculator(new CommandFactory());
    }

    private static String buildStressProgram(int opCount) {
        StringBuilder sb = new StringBuilder(opCount * 10);
        sb.append("PUSH 1\n");
        for (int i = 0; i < opCount; i++) {
            sb.append("PUSH 1\n+\n");
        }
        return sb.toString();
    }
}
