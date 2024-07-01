package org.datavaultplatform.webapp.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Information ONLY : Used to label methods that have @PreAuthorize expression which uses 'retrievesPaused()'
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface UsesRetrievesPaused {
}
