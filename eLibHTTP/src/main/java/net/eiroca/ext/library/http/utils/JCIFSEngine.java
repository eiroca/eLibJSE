/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio) - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.ext.library.http.utils;

import java.io.IOException;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;
import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;

/**
 * Authentication engine which uses the JCIFS lib to create NTLM messages
 */
public final class JCIFSEngine implements NTLMEngine {

  private static final int TYPE_1_FLAGS = NtlmFlags.NTLMSSP_NEGOTIATE_56 | NtlmFlags.NTLMSSP_NEGOTIATE_128 | NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN | NtlmFlags.NTLMSSP_REQUEST_TARGET;

  @Override
  public String generateType1Msg(final String domain, final String workstation) throws NTLMEngineException {
    final Type1Message type1Message = new Type1Message(JCIFSEngine.TYPE_1_FLAGS, domain, workstation);
    return Base64.encode(type1Message.toByteArray());
  }

  @Override
  public String generateType3Msg(final String username, final String password, final String domain, final String workstation, final String challenge) throws NTLMEngineException {
    Type2Message type2Message;
    try {
      type2Message = new Type2Message(Base64.decode(challenge));
    }
    catch (final IOException exception) {
      throw new NTLMEngineException("Invalid NTLM type 2 message", exception);
    }
    final int type2Flags = type2Message.getFlags();
    final int type3Flags = type2Flags & (0xffffffff ^ (NtlmFlags.NTLMSSP_TARGET_TYPE_DOMAIN | NtlmFlags.NTLMSSP_TARGET_TYPE_SERVER));
    final Type3Message type3Message = new Type3Message(type2Message, password, domain, username, workstation, type3Flags);
    return Base64.encode(type3Message.toByteArray());
  }

}