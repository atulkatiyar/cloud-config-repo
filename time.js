function displayLogInfo(logData) {
            const logEntries = $('#logTableBody');
            logEntries.empty();

            const logData1 = logData.data;
            if(Object.keys(logData1).length == 0) {
                $('#logContainer').hide();
                return;
            }

            $('#logContainer').show();
            const sortedKeys = Object.keys(logData1).sort();

            sortedKeys.forEach(key => {
                if (key === 'remainingTime') return;

                const value = logData1[key];
                const row = $('<tr>');
                const loggerKey = key == 'packageLogLevels' ? 'Package Logging Level' : 'Root Logging Level';
                row.append($('<td>').text(loggerKey));
                const loggerKeys = Object.keys(value);

                if(key == 'packageLogLevels') {
                const col = $('<td>');
                 loggerKeys.forEach(key1 => {
                     const row1 = $('<tr>');
                     const value1 = value[key1];
                     row1.append($('<td>').addClass('log-value-cell').text(key1.concat(": ", value1.logLevel)));
                     const userId = value1.userId || '';
                     col.append(row1);
                     row.append(col);
                     row.append($('<td>').addClass('log-value-cell').text(formatValue(userId)));
                     const endTime = value1.timer.endTime;
                     const nowMillis = Date.now();
                     const diffInSeconds = (endTime - Date.now()) % 1000;
                     if(diffInSeconds > 0) {
                        row.append($('<td>').addClass('log-value-cell').text(diffInSeconds + ' seconds'));
                     } else{
                        row.append($('<td>').addClass('log-value-cell').text(''));
                     }


                 });
                } else{
                    const userId = value.rootLevel.userId || '';
                    row.append($('<td>').addClass('log-value-cell').text(value.rootLevel.logLevel));
                    row.append($('<td>').addClass('log-value-cell').text(formatValue(userId)));
                    const endTime = value.rootLevel.timer.endTime ;
                    const nowMillis = Date.now();
                     const diffInSeconds = endTime - Date.now();
                     if(diffInSeconds > 0) {
                        row.append($('<td>').addClass('log-value-cell').text(diffInSeconds + ' seconds'));
                     } else{
                        row.append($('<td>').addClass('log-value-cell').text(''));
                     }
                }

                logEntries.append(row);
            });
        }
