package com.uit.buddy.entity.social;

import com.uit.buddy.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostMedia {
  private FileType type;
  private String url;
}
