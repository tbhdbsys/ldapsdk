/*
 * Copyright 2014-2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2015-2016 UnboundID Corp.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk.unboundidds.extensions;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.unboundid.asn1.ASN1Element;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.asn1.ASN1Sequence;
import com.unboundid.asn1.ASN1Set;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.ExtendedRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Debug;
import com.unboundid.util.NotMutable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.Validator;

import static com.unboundid.ldap.sdk.unboundidds.extensions.ExtOpMessages.*;



/**
 * This class provides an extended request that may be used to retrieve a list
 * of the subscriptions associated with a specified notification manager,
 * optionally restricted to a specified set of destinations.
 * <BR>
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * <BR>
 * The request has an OID of 1.3.6.1.4.1.30221.2.6.40 and a value with the
 * following encoding: <BR><BR>
 * <PRE>
 *   ListNotificationSubscriptionsRequest ::= SEQUENCE {
 *        notificationManagerID          OCTET STRING,
 *        notificationDestinationIDs     SET OF OCTET STRING OPTIONAL }
 * </PRE>
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class ListNotificationSubscriptionsExtendedRequest
       extends ExtendedRequest
{
  /**
   * The OID (1.3.6.1.4.1.30221.2.6.40) for the list notification subscriptions
   * extended request.
   */
  public static final String LIST_NOTIFICATION_SUBSCRIPTIONS_REQUEST_OID =
       "1.3.6.1.4.1.30221.2.6.40";



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -8124073083247944273L;



  // The notification destination IDs.
  private final Set<String> destinationIDs;

  // The notification manager ID.
  private final String managerID;



  /**
   * Creates a new list notification subscriptions extended request with the
   * provided information.
   *
   * @param  managerID          The notification manager ID.  It must not be
   *                            {@code null}.
   * @param  destinationIDs     The set of notification destination IDs for
   *                            which to retrieve the subscription information.
   *                            It may be {@code null} or empty if subscription
   *                            information for all destinations should be
   *                            returned.
   */
  public ListNotificationSubscriptionsExtendedRequest(final String managerID,
              final String... destinationIDs)
  {
    this(managerID, StaticUtils.toList(destinationIDs));
  }



  /**
   * Creates a new list notification subscriptions extended request with the
   * provided information.
   *
   * @param  managerID          The notification manager ID.  It must not be
   *                            {@code null}.
   * @param  destinationIDs     The set of notification destination IDs for
   *                            which to retrieve the subscription information.
   *                            It may be {@code null} or empty if subscription
   *                            information for all destinations should be
   *                            returned.
   * @param  controls           The set of controls to include in the request.
   *                            It may be {@code null} or empty if no controls
   *                            are needed.
   */
  public ListNotificationSubscriptionsExtendedRequest(final String managerID,
              final Collection<String> destinationIDs,
              final Control... controls)
  {
    super(LIST_NOTIFICATION_SUBSCRIPTIONS_REQUEST_OID,
         encodeValue(managerID, destinationIDs), controls);

    this.managerID = managerID;

    if (destinationIDs == null)
    {
      this.destinationIDs = Collections.emptySet();
    }
    else
    {
      this.destinationIDs = Collections.unmodifiableSet(
           new LinkedHashSet<String>(destinationIDs));
    }
  }



  /**
   * Creates a new list notification subscriptions extended request from the
   * provided generic extended request.
   *
   * @param  extendedRequest  The generic extended request to use to create this
   *                          list notification subscriptions extended request.
   *
   * @throws  LDAPException  If a problem occurs while decoding the request.
   */
  public ListNotificationSubscriptionsExtendedRequest(
              final ExtendedRequest extendedRequest)
         throws LDAPException
  {
    super(extendedRequest);

    final ASN1OctetString value = extendedRequest.getValue();
    if (value == null)
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_LIST_NOTIFICATION_SUBS_REQ_DECODE_NO_VALUE.get());
    }

    try
    {
      final ASN1Element[] elements =
           ASN1Sequence.decodeAsSequence(value.getValue()).elements();
      managerID =
           ASN1OctetString.decodeAsOctetString(elements[0]).stringValue();

      if (elements.length > 1)
      {
        final ASN1Element[] destIDElements =
             ASN1Sequence.decodeAsSequence(elements[1]).elements();

        final LinkedHashSet<String> destIDs =
             new LinkedHashSet<String>(destIDElements.length);
        for (final ASN1Element e : destIDElements)
        {
          destIDs.add(ASN1OctetString.decodeAsOctetString(e).stringValue());
        }
        destinationIDs = Collections.unmodifiableSet(destIDs);
      }
      else
      {
        destinationIDs = Collections.emptySet();
      }
    }
    catch (final Exception e)
    {
      Debug.debugException(e);
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_LIST_NOTIFICATION_SUBS_REQ_ERROR_DECODING_VALUE.get(
                StaticUtils.getExceptionMessage(e)),
           e);
    }
  }



  /**
   * Encodes the provided information into an ASN.1 octet string suitable for
   * use as the value of this extended request.
   *
   * @param  managerID          The notification manager ID.  It must not be
   *                            {@code null}.
   * @param  destinationIDs     The set of notification destination IDs for
   *                            which to retrieve the subscription information.
   *                            It may be {@code null} or empty if subscription
   *                            information for all destinations should be
   *                            returned.
   *
   * @return  The ASN.1 octet string containing the encoded value.
   */
  private static ASN1OctetString encodeValue(final String managerID,
                      final Collection<String> destinationIDs)
  {
    Validator.ensureNotNull(managerID);

    final ArrayList<ASN1Element> elements = new ArrayList<ASN1Element>(2);
    elements.add(new ASN1OctetString(managerID));

    if ((destinationIDs != null) && (! destinationIDs.isEmpty()))
    {
      final LinkedHashSet<ASN1Element> destIDElements =
           new LinkedHashSet<ASN1Element>(destinationIDs.size());
      for (final String destinationID : destinationIDs)
      {
        destIDElements.add(new ASN1OctetString(destinationID));
      }
      elements.add(new ASN1Set(destIDElements));
    }

    return new ASN1OctetString(new ASN1Sequence(elements).encode());
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public ListNotificationSubscriptionsExtendedResult
              process(final LDAPConnection connection, final int depth)
         throws LDAPException
  {
    final ExtendedResult extendedResponse = super.process(connection, depth);
    return new ListNotificationSubscriptionsExtendedResult(extendedResponse);
  }



  /**
   * Retrieves the notification manager ID.
   *
   * @return  The notification manager ID.
   */
  public String getManagerID()
  {
    return managerID;
  }



  /**
   * Retrieves the notification destination IDs, if any were provided.
   *
   * @return  The notification destination IDs, or an empty set if none were
   *          provided.
   */
  public Set<String> getDestinationIDs()
  {
    return destinationIDs;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public ListNotificationSubscriptionsExtendedRequest duplicate()
  {
    return duplicate(getControls());
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public ListNotificationSubscriptionsExtendedRequest
              duplicate(final Control[] controls)
  {
    final ListNotificationSubscriptionsExtendedRequest r =
         new ListNotificationSubscriptionsExtendedRequest(managerID,
              destinationIDs, controls);
    r.setResponseTimeoutMillis(getResponseTimeoutMillis(null));
    return r;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getExtendedRequestName()
  {
    return INFO_EXTENDED_REQUEST_NAME_LIST_NOTIFICATION_SUBS.get();
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toString(final StringBuilder buffer)
  {
    buffer.append("ListNotificationSubscriptionsExtendedRequest(managerID='");
    buffer.append(managerID);
    buffer.append('\'');

    if (! destinationIDs.isEmpty())
    {
      buffer.append(", destinationIDs={");

      final Iterator<String> iterator = destinationIDs.iterator();
      while (iterator.hasNext())
      {
        buffer.append('\'');
        buffer.append(iterator.next());
        buffer.append('\'');

        if (iterator.hasNext())
        {
          buffer.append(", ");
        }
      }

      buffer.append('}');
    }

    final Control[] controls = getControls();
    if (controls.length > 0)
    {
      buffer.append(", controls={");
      for (int i=0; i < controls.length; i++)
      {
        if (i > 0)
        {
          buffer.append(", ");
        }

        buffer.append(controls[i]);
      }
      buffer.append('}');
    }

    buffer.append(')');
  }
}
