/*
 * Copyright 2007-2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2008-2016 UnboundID Corp.
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
package com.unboundid.asn1;



import com.unboundid.util.NotMutable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.asn1.ASN1Constants.*;
import static com.unboundid.asn1.ASN1Messages.*;
import static com.unboundid.util.Debug.*;
import static com.unboundid.util.StaticUtils.*;



/**
 * This class provides an ASN.1 null element, which does not hold a value.  Null
 * elements are generally used as placeholders that can be substituted for other
 * types of elements.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class ASN1Null
       extends ASN1Element
{
  /**
   * A pre-allocated ASN.1 null element with the universal null BER type.
   */
  public static final ASN1Null UNIVERSAL_NULL_ELEMENT = new ASN1Null();



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -3264450066845549348L;



  /**
   * Creates a new ASN.1 null element with the default BER type.
   */
  public ASN1Null()
  {
    super(UNIVERSAL_NULL_TYPE);
  }



  /**
   * Creates a new ASN.1 null element with the specified BER type.
   *
   * @param  type  The BER type to use for this ASN.1 null element.
   */
  public ASN1Null(final byte type)
  {
    super(type);
  }



  /**
   * Decodes the contents of the provided byte array as a null element.
   *
   * @param  elementBytes  The byte array to decode as an ASN.1 null element.
   *
   * @return  The decoded ASN.1 null element.
   *
   * @throws  ASN1Exception  If the provided array cannot be decoded as a null
   *                         element.
   */
  public static ASN1Null decodeAsNull(final byte[] elementBytes)
         throws ASN1Exception
  {
    try
    {
      int valueStartPos = 2;
      int length = (elementBytes[1] & 0x7F);
      if (length != elementBytes[1])
      {
        final int numLengthBytes = length;

        length = 0;
        for (int i=0; i < numLengthBytes; i++)
        {
          length <<= 8;
          length |= (elementBytes[valueStartPos++] & 0xFF);
        }
      }

      if ((elementBytes.length - valueStartPos) != length)
      {
        throw new ASN1Exception(ERR_ELEMENT_LENGTH_MISMATCH.get(length,
                                     (elementBytes.length - valueStartPos)));
      }

      if (length != 0)
      {
        throw new ASN1Exception(ERR_NULL_HAS_VALUE.get());
      }

      return new ASN1Null(elementBytes[0]);
    }
    catch (final ASN1Exception ae)
    {
      debugException(ae);
      throw ae;
    }
    catch (final Exception e)
    {
      debugException(e);
      throw new ASN1Exception(ERR_ELEMENT_DECODE_EXCEPTION.get(e), e);
    }
  }



  /**
   * Decodes the provided ASN.1 element as a null element.
   *
   * @param  element  The ASN.1 element to be decoded.
   *
   * @return  The decoded ASN.1 null element.
   *
   * @throws  ASN1Exception  If the provided element cannot be decoded as a null
   *                         element.
   */
  public static ASN1Null decodeAsNull(final ASN1Element element)
         throws ASN1Exception
  {
    if (element.getValue().length != 0)
    {
      throw new ASN1Exception(ERR_NULL_HAS_VALUE.get());
    }

    return new ASN1Null(element.getType());
  }



  /**
   * Appends a string representation of this ASN.1 element to the provided
   * buffer.
   *
   * @param  buffer  The buffer to which to append the information.
   */
  @Override()
  public void toString(final StringBuilder buffer)
  {
    buffer.append("ASN1Null(type=");
    toHex(getType(), buffer);
    buffer.append(')');
  }
}
