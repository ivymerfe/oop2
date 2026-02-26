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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class CalculatorLoadBenchmark {

    private Calculator calculator;
    private String longProgram;

    @Setup(org.openjdk.jmh.annotations.Level.Trial)
    public void setupTrial() {
        calculator = createCalculatorWithCommands();
        longProgram = buildLongProgram(2_000);
    }

    @Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setupInvocation() {
        calculator.getStack().clear();
        calculator.getVariables().clear();
    }

    @Benchmark
    public void shortExpressionProgram() throws CommandException {
        calculator.execute("PUSH 10; PUSH 20; +; PUSH 2; /; PUSH 3; *");
    }

    @Benchmark
    public void programmaticCommandCalls() throws CommandException {
        calculator.executeCommand("PUSH", "10");
        calculator.executeCommand("PUSH", "20");
        calculator.executeCommand("+");
        calculator.executeCommand("PUSH", "2");
        calculator.executeCommand("/");
        calculator.executeCommand("PUSH", "3");
        calculator.executeCommand("*");
    }

    @Benchmark
    public void longProgramBatch() throws CommandException {
        calculator.execute(longProgram);
    }

    private static Calculator createCalculatorWithCommands() {
        return new Calculator(new CommandFactory());
    }

    private static String buildLongProgram(int operations) {
        StringBuilder sb = new StringBuilder();
        sb.append("PUSH 1\n");
        for (int i = 0; i < operations; i++) {
            sb.append("PUSH 1\n+\n");
        }
        return sb.toString();
    }
}
