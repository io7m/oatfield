/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.oatfield.vanilla.internal;

import com.io7m.oatfield.api.OFBundleResolutionException;
import com.io7m.oatfield.api.OFBundleResolverConfiguration;
import com.io7m.oatfield.api.OFBundleResolverType;
import org.apache.felix.resolver.ResolverImpl;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;
import org.osgi.service.resolver.HostedCapability;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.ResolveContext;
import org.osgi.service.resolver.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The default bundle resolver.
 */

public final class OFBundleResolver implements OFBundleResolverType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OFBundleResolver.class);

  private final OFBundleResolverConfiguration configuration;

  /**
   * The default bundle resolver.
   *
   * @param inConfiguration The resolver configuration
   */

  public OFBundleResolver(
    final OFBundleResolverConfiguration inConfiguration)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
  }

  @Override
  public Map<Resource, List<Wire>> execute()
    throws OFBundleResolutionException
  {
    final var context = new Context();
    context.resources.addAll(this.configuration.availableResources());
    context.mandatory.addAll(this.configuration.mandatoryResources());
    context.optional.addAll(this.configuration.optionalResources());

    final Resolver resolver =
      new ResolverImpl(new ResolverLogger());

    try {
      return resolver.resolve(context);
    } catch (final ResolutionException e) {
      throw new OFBundleResolutionException(
        e, List.copyOf(e.getUnresolvedRequirements())
      );
    }
  }

  private static final class Context extends ResolveContext
  {
    private final List<Resource> resources;
    private final Map<Resource, Wiring> wirings;
    private final Collection<Resource> mandatory;
    private final Collection<Resource> optional;

    Context()
    {
      this.resources = new ArrayList<>();
      this.wirings = new HashMap<>();
      this.mandatory = new ArrayList<>();
      this.optional = new ArrayList<>();
    }

    @Override
    public Collection<Resource> getMandatoryResources()
    {
      return new ArrayList<>(this.mandatory);
    }

    @Override
    public Collection<Resource> getOptionalResources()
    {
      return new ArrayList<>(this.optional);
    }

    @Override
    public List<Capability> findProviders(
      final Requirement requirement)
    {
      final List<Capability> capabilities = new ArrayList<>();
      for (final var resource : this.resources) {
        for (final var capability : resource.getCapabilities(requirement.getNamespace())) {
          if (requirementMatchesCapability(requirement, capability)) {
            capabilities.add(capability);
          }
        }
      }

      return capabilities;
    }

    private static boolean requirementMatchesCapability(
      final Requirement requirement,
      final Capability capability)
    {
      if (requirement == null && capability == null) {
        return true;
      }

      if (requirement == null || capability == null) {
        return false;
      }

      if (!Objects.equals(
        capability.getNamespace(),
        requirement.getNamespace())) {
        return false;
      }

      final var filter = requirement.getDirectives().get(Constants.FILTER_DIRECTIVE);
      if (filter == null) {
        return true;
      }

      try {
        if (FrameworkUtil.createFilter(filter).matches(capability.getAttributes())) {
          return true;
        }
      } catch (final InvalidSyntaxException e) {
        return false;
      }

      return false;
    }

    @Override
    public int insertHostedCapability(
      final List<Capability> capabilities,
      final HostedCapability capability)
    {
      capabilities.add(0, capability);
      return 0;
    }

    @Override
    public boolean isEffective(
      final Requirement requirement)
    {
      return true;
    }

    @Override
    public Map<Resource, Wiring> getWirings()
    {
      return this.wirings;
    }
  }

  private static final class ResolverLogger
    extends org.apache.felix.resolver.Logger
  {
    ResolverLogger()
    {
      super(5);
    }

    @Override
    protected void doLog(
      final int level,
      final String msg,
      final Throwable throwable)
    {
      switch (level) {
        case LOG_ERROR -> {
          if (throwable != null) {
            LOG.error("{}: ", msg, throwable);
          } else {
            LOG.error("{}", msg);
          }
        }
        case LOG_DEBUG -> {
          if (throwable != null) {
            LOG.debug("{}: ", msg, throwable);
          } else {
            LOG.debug("{}", msg);
          }
        }
        case LOG_INFO -> {
          if (throwable != null) {
            LOG.info("{}: ", msg, throwable);
          } else {
            LOG.info("{}", msg);
          }
        }
        case LOG_WARNING -> {
          if (throwable != null) {
            LOG.warn("{}: ", msg, throwable);
          } else {
            LOG.warn("{}", msg);
          }
        }
        default -> {
          
        }
      }
    }
  }
}
