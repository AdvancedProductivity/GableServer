package org.advancedproductivity.gable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TestZ {

    public static void main(String[] args) {
        handle(5);
    }

    private static void handle(int n) {
        final Stream<Double> doubleStream = Stream.iterate(0, x -> x + 1).limit(n).map(x -> Math.pow(10d, x));
        final List<Double> collect = doubleStream.collect(Collectors.toList());
        for (int i = 0; i < 10; i++) {
            int value = 0;
            for (int j = 0; j < collect.size(); j++) {
                final Double aDouble = collect.get(j);
                value += (aDouble + i);
            }
            System.out.println(value);
        }
    }
}
