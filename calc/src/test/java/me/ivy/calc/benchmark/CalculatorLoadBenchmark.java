package me.ivy.calc.benchmark;

import me.ivy.basic_commands.DefineCommand;
import me.ivy.basic_commands.PopCommand;
import me.ivy.basic_commands.PushCommand;
import me.ivy.basic_commands.SqrtCommand;
import me.ivy.binary_commands.BinaryCommand;
import me.ivy.calc.Calculator;
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
@Fork(value = 1, jvmArgsAppend = {
    "-Dlog4j2.configurationFile=log4j2-benchmark.xml",
    "-Dlog4j2.statusLoggerLevel=OFF"
})
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
    public String shortExpressionProgram() {
        return calculator.execute("PUSH 10; PUSH 20; +; PUSH 2; /; PUSH 3; *");
    }

    @Benchmark
    public String programmaticCommandCalls() {
        calculator.executeCommand("PUSH", "10");
        calculator.executeCommand("PUSH", "20");
        calculator.executeCommand("+");
        calculator.executeCommand("PUSH", "2");
        calculator.executeCommand("/");
        calculator.executeCommand("PUSH", "3");
        return calculator.executeCommand("*");
    }

    @Benchmark
    public String longProgramBatch() {
        return calculator.execute(longProgram);
    }

    private static Calculator createCalculatorWithCommands() {
        Calculator calc = new Calculator();
        calc.registerCommand("PUSH", new PushCommand());
        calc.registerCommand("POP", new PopCommand());
        calc.registerCommand("DEFINE", new DefineCommand());
        calc.registerCommand("SQRT", new SqrtCommand());
        calc.registerCommand("+", new BinaryCommand("+"));
        calc.registerCommand("-", new BinaryCommand("-"));
        calc.registerCommand("*", new BinaryCommand("*"));
        calc.registerCommand("/", new BinaryCommand("/"));
        return calc;
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
