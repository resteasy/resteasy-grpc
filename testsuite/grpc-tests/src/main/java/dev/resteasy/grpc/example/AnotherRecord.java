package dev.resteasy.grpc.example;

import java.util.List;

public record AnotherRecord<T>(String s, List<T> content) {
}