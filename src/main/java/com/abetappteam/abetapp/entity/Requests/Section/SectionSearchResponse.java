package com.abetappteam.abetapp.entity.Requests.Section;


import com.abetappteam.abetapp.entity.*;

import java.util.List;

public record SectionSearchResponse(
        List<Section> sections,
        List<Course> courses
) {
}
