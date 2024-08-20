# Use the official PostgreSQL image as the base image
FROM postgres:14

# Set environment variables
ENV POSTGRES_DB=marketcove_db
ENV POSTGRES_USER=daniel
ENV POSTGRES_PASSWORD=changeit

# Copy initialization scripts
#COPY ./init.sql /docker-entrypoint-initdb.d/

# Expose the PostgreSQL port
EXPOSE 5432