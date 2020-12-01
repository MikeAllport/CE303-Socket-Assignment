using System;
using System.Collections.Generic;
using System.Text;

namespace Server
{
    /**
     * Simple exception class for when trader attempting to reconnect but server not in
     * restarting state
     */
    class ServerNotRestartingException: Exception
    {
        public ServerNotRestartingException(String message) : base(message) { }
    }
}
