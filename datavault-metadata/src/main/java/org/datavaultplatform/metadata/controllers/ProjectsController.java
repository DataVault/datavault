package org.datavaultplatform.metadata.controllers;

import java.util.List;

import org.datavaultplatform.common.metadata.Project;
import org.datavaultplatform.metadata.services.ProjectsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectsController {
    
    private ProjectsService projectsService;
    
    public void setProjectsService(ProjectsService projectsService) {
        this.projectsService = projectsService;
    }
    
    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    public List<Project> getProjects() {
        return projectsService.getProjects();
    }
}
