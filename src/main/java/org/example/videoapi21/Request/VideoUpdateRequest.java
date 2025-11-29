package org.example.videoapi21.Request;

import java.util.Optional;

public record VideoUpdateRequest (Optional<String> title, Optional<String> description){
}
