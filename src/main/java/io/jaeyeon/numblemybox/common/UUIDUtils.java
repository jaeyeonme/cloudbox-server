package io.jaeyeon.numblemybox.common;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UUIDUtils {

	public String getUUID() {
		return UUID.randomUUID().toString();
	}
}
